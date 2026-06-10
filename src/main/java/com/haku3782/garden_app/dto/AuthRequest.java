package com.haku3782.garden_app.dto;

import lombok.Data;

/**
 * ログイン API へのリクエスト body をマッピングするクラス。
 *
 * <p>クライアントが POST /api/auth/login を呼び出す際に
 * JSON として送信するユーザー名とパスワードを受け取る。
 *
 * <p>【@Data が自動生成するもの】
 * <ul>
 *   <li>getter / setter : 各フィールドへのアクセサ</li>
 *   <li>equals / hashCode : 同値比較</li>
 *   <li>toString : デバッグ用の文字列表現</li>
 * </ul>
 */
@Data
public class AuthRequest {
    /** ログインするユーザーのユーザー名 */
    private String username;

    /** ログインするユーザーのパスワード（平文で受け取り、サービス層でハッシュ検証する） */
    private String password;
}
