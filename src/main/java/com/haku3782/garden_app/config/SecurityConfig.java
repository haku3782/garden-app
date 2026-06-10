package com.haku3782.garden_app.config;

import com.haku3782.garden_app.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security の設定を定義するクラス。
 *
 * <p>アプリケーション全体の認証・認可ルール、JWT フィルターの組み込み、
 * パスワードエンコーダーの設定をまとめて管理する。
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@Configuration    : このクラスが Bean 定義を含む設定クラスであることを示す</li>
 *   <li>@EnableWebSecurity: Spring Security の Web セキュリティ機能を有効化する</li>
 *   <li>@RequiredArgsConstructor : final フィールドへの依存性注入用コンストラクタを自動生成（Lombok）</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT 認証を行うフィルター */
    private final JwtFilter jwtFilter;

    /**
     * HTTP セキュリティの設定を定義する Bean。
     *
     * <p>設定内容：
     * <ul>
     *   <li>CSRF 無効化       : REST API では Cookie を使わないため不要</li>
     *   <li>セッション管理     : STATELESS にすることでサーバー側にセッションを保持しない
     *                           （JWT で認証するため）</li>
     *   <li>認可ルール        : /api/auth/** は誰でもアクセス可能。それ以外は認証必須</li>
     *   <li>JWT フィルター追加 : UsernamePasswordAuthenticationFilter の前に JwtFilter を挿入し、
     *                           リクエストごとにトークンを検証する</li>
     * </ul>
     *
     * @param http HttpSecurity の設定ビルダー
     * @return 設定済みの SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 無効化（REST API のため不要）
            .csrf(AbstractHttpConfigurer::disable)
            // セッションを使わない（JWT 認証のため STATELESS に設定）
            .sessionManagement(s -> s
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 認可ルールの設定
            .authorizeHttpRequests(auth -> auth
                // /api/auth/** （登録・ログイン）は認証なしでアクセス可
                .requestMatchers("/api/auth/**").permitAll()
                // それ以外のエンドポイントは認証必須
                .anyRequest().authenticated())
            // JwtFilter を Spring Security のフィルターチェーンに追加
            .addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * パスワードのハッシュ化・照合に使用する Bean。
     *
     * <p>BCrypt アルゴリズムを使用する。
     * BCrypt はソルト付きハッシュを自動生成するため、
     * 同じパスワードでも毎回異なるハッシュ値になり安全性が高い。
     * AuthService の passwordEncoder.encode() / matches() で使われる。
     *
     * @return BCryptPasswordEncoder のインスタンス
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
