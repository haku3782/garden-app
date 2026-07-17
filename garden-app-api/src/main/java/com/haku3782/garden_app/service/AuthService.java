package com.haku3782.garden_app.service;

import com.haku3782.garden_app.domain.User;
import com.haku3782.garden_app.dto.AuthRequest;
import com.haku3782.garden_app.dto.AuthResponse;
import com.haku3782.garden_app.repository.UserRepository;
import com.haku3782.garden_app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 認証（登録・ログイン）に関するビジネスロジックを担当するサービスクラス。
 *
 * <p>Controller から呼び出され、ユーザー登録とログインの処理を行う。
 * 成功した場合は JWT を生成して返し、失敗した場合は例外をスローする。
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@Service           : Spring のサービス層コンポーネントとして登録</li>
 *   <li>@RequiredArgsConstructor : final フィールドを引数に取るコンストラクタを自動生成（Lombok）。
 *                                  Spring がそのコンストラクタを使って依存関係を注入する</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /** users テーブルへのデータアクセスを担うリポジトリ */
    private final UserRepository userRepository;

    /** パスワードのハッシュ化と照合を行うエンコーダー（BCrypt） */
    private final PasswordEncoder passwordEncoder;

    /** JWT の生成・検証を行うユーティリティ */
    private final JwtUtil jwtUtil;

    /**
     * 新規ユーザーを登録し、JWT を返す。
     *
     * <p>処理の流れ：
     * <ol>
     *   <li>同じユーザー名がすでに存在する場合は例外をスロー</li>
     *   <li>パスワードをハッシュ化して User エンティティを生成・保存</li>
     *   <li>登録したユーザー名で JWT を生成して返す</li>
     * </ol>
     *
     * @param request ユーザー名とパスワードを含むリクエスト
     * @return 発行した JWT を含むレスポンス
     * @throws RuntimeException ユーザー名がすでに使われている場合
     */
    public AuthResponse register(AuthRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        if (username == null || username.length() < 3 || username.length() > 30) {
            throw new RuntimeException("ユーザー名は3〜30文字で入力してください");
        }
        if (password == null || password.length() < 8 || password.length() > 100) {
            throw new RuntimeException("パスワードは8〜100文字で入力してください");
        }
        // 同名ユーザーの重複チェック
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("このユーザー名はすでに使われています");
        }
        // パスワードをハッシュ化して保存
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        // JWT を生成して返す
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token);
    }

    /**
     * ユーザー名とパスワードを照合してログインし、JWT を返す。
     *
     * <p>処理の流れ：
     * <ol>
     *   <li>ユーザー名で DB を検索し、存在しない場合は例外をスロー</li>
     *   <li>入力パスワードと DB のハッシュを照合し、不一致なら例外をスロー</li>
     *   <li>認証成功後に JWT を生成して返す</li>
     * </ol>
     *
     * @param request ユーザー名とパスワードを含むリクエスト
     * @return 発行した JWT を含むレスポンス
     * @throws RuntimeException ユーザーが存在しない場合、またはパスワードが不一致の場合
     */
    public AuthResponse login(AuthRequest request) {
        // ユーザー名でユーザーを検索（存在しなければ例外）
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        // 入力パスワードと DB のハッシュを照合
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("パスワードが違います");
        }
        // JWT を生成して返す
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token);
    }
}
