package com.haku3782.garden_app.security;

import com.haku3782.garden_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Spring Security が認証処理で使用するユーザー情報取得サービス。
 *
 * <p>UserDetailsService インターフェースを実装し、
 * ユーザー名を元に DB からユーザーを検索して Spring Security が扱える
 * UserDetails 形式に変換して返す。
 *
 * <p>このクラスを SecurityConfig から切り出すことで、
 * SecurityConfig ↔ JwtFilter 間の循環依存を解消している。
 *
 * <p>【依存関係の変化】
 * <pre>
 * 【修正前・循環あり】
 * SecurityConfig → JwtFilter → UserDetailsService（SecurityConfig の @Bean）
 *       ↑___________________________________________________|
 *
 * 【修正後・循環なし】
 * SecurityConfig → JwtFilter → CustomUserDetailsService → UserRepository
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /** users テーブルへのデータアクセスを担うリポジトリ */
    private final UserRepository userRepository;

    /**
     * ユーザー名で DB を検索し、Spring Security が扱える UserDetails を返す。
     *
     * <p>JwtFilter および Spring Security の認証フローから呼び出される。
     * ユーザーが存在しない場合は UsernameNotFoundException をスローする。
     *
     * @param username 検索するユーザー名
     * @return Spring Security の UserDetails オブジェクト
     * @throws UsernameNotFoundException ユーザーが DB に存在しない場合
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities(new ArrayList<>())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
