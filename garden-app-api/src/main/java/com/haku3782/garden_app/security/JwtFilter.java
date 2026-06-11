package com.haku3782.garden_app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * すべての HTTP リクエストに対して JWT 認証を行うフィルタークラス。
 *
 * <p>OncePerRequestFilter を継承することで、1リクエストにつき必ず1回だけ実行される。
 * リクエストヘッダーから JWT を取り出し、有効であれば Spring Security の
 * SecurityContext に認証情報をセットする。
 *
 * <p>【処理の流れ】
 * <pre>
 * HTTPリクエスト
 *     ↓
 * Authorization ヘッダーを取得
 *     ↓ "Bearer {token}" 形式か確認
 * JWT を検証（署名・有効期限）
 *     ↓ 有効な場合
 * ユーザー情報を DB から取得
 *     ↓
 * SecurityContext に認証情報をセット
 *     ↓
 * 次のフィルター or コントローラーへ
 * </pre>
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@Component           : Spring のコンポーネントとして登録し、DI 可能にする</li>
 *   <li>@RequiredArgsConstructor : final フィールドへの依存性注入用コンストラクタを自動生成（Lombok）</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    /** JWT の生成・検証を行うユーティリティ */
    private final JwtUtil jwtUtil;

    /** ユーザー名を元に DB からユーザー情報を取得するサービス */
    private final UserDetailsService userDetailsService;

    /**
     * リクエストごとに JWT 認証を行うメインメソッド。
     *
     * <p>処理の流れ：
     * <ol>
     *   <li>Authorization ヘッダーを取得し、"Bearer " で始まるか確認</li>
     *   <li>トークン部分（"Bearer " の後ろ7文字以降）を切り出す</li>
     *   <li>JwtUtil でトークンの署名・有効期限を検証</li>
     *   <li>トークンからユーザー名を取り出し、DB のユーザー情報を取得</li>
     *   <li>SecurityContext に認証情報をセットすることで、以降の処理で「認証済み」と判断される</li>
     *   <li>次のフィルターまたはコントローラーへ処理を渡す（filterChain.doFilter）</li>
     * </ol>
     *
     * <p>Authorization ヘッダーがない場合や JWT が無効な場合は、
     * 認証情報をセットせずそのまま次の処理へ渡す。
     * （未認証のため、保護されたエンドポイントへのアクセスは SecurityConfig でブロックされる）
     *
     * @param request     HTTP リクエスト
     * @param response    HTTP レスポンス
     * @param filterChain 次のフィルターへ処理を渡すチェーン
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization ヘッダーを取得
        String authHeader = request.getHeader("Authorization");

        // "Bearer {token}" 形式の場合のみ処理する
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // "Bearer " の7文字を取り除いてトークン本体を取り出す
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                // トークンからユーザー名を取り出し、DB のユーザー情報を取得
                String username = jwtUtil.extractUsername(token);
                var userDetails = userDetailsService.loadUserByUsername(username);
                // Spring Security 6+ の推奨方法：新しい SecurityContext を明示的に生成してセット
                // （旧方式の getContext().setAuthentication() は Spring Security 7 で正しく伝播しない）
                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            }
        }
        // 次のフィルターまたはコントローラーへ処理を渡す
        filterChain.doFilter(request, response);
    }
}
