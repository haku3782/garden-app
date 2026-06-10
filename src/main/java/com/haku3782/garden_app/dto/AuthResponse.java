package com.haku3782.garden_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ログイン成功時にクライアントへ返すレスポンス body クラス。
 *
 * <p>認証が通った後、発行した JWT をそのままフィールドに持ち、
 * JSON {"token": "..."} の形式でクライアントに返す。
 *
 * <p>【アノテーションの効果】
 * <ul>
 *   <li>@Data            : getter / setter / equals / hashCode / toString を自動生成</li>
 *   <li>@AllArgsConstructor : 全フィールドを引数に取るコンストラクタを自動生成
 *                            （例：new AuthResponse(token) で生成可能）</li>
 * </ul>
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    /** クライアントに返す JWT 文字列 */
    private String token;
}
