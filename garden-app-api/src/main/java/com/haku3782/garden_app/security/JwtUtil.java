package com.haku3782.garden_app.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT（JSON Web Token）の生成・検証を担当するユーティリティクラス。
 *
 * <p>ログイン成功時にトークンを発行し、以降のリクエストで
 * トークンの正当性を確認するために使用する。
 *
 * <p>【秘密鍵の管理】
 * SECRET はコードに直接書かず、環境変数 → application.yaml → @Value の流れで注入する。
 * ローカルは application.local.yaml、本番は環境変数 JWT_SECRET で設定する。
 */
@Component
public class JwtUtil {

    /**
     * 署名に使う秘密鍵の文字列（最低32バイト必要）。
     * application.yaml の jwt.secret を経由して環境変数 JWT_SECRET から注入される。
     */
    @Value("${jwt.secret}")
    private String secret;

    /** トークンの有効期限：24時間（ミリ秒） */
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24時間

    /**
     * secret フィールドから HMAC-SHA 用の SecretKey オブジェクトを生成する。
     * トークンの署名・検証の両方で共通して使用する。
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * ユーザー名を元に JWT を生成する。
     *
     * <p>トークンには以下の情報が含まれる：
     * <ul>
     *   <li>subject  : ユーザー名</li>
     *   <li>issuedAt : 発行日時</li>
     *   <li>expiration: 有効期限（発行から24時間後）</li>
     * </ul>
     *
     * @param username トークンに埋め込むユーザー名
     * @return 署名済み JWT 文字列
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    /**
     * トークンからユーザー名（subject）を取り出す。
     *
     * <p>署名の検証も同時に行われるため、改ざんされたトークンは
     * この時点で例外がスローされる。
     *
     * @param token JWT 文字列
     * @return トークンに含まれるユーザー名
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * トークンの署名・有効期限を検証する。
     *
     * <p>内部で extractUsername を呼び出し、例外が発生しなければ有効と判断する。
     * 改ざん・期限切れ・不正な形式の場合は false を返す。
     *
     * @param token 検証対象の JWT 文字列
     * @return 有効なら true、無効なら false
     */
    public boolean validateToken(String token) {
        try {
            extractUsername(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
