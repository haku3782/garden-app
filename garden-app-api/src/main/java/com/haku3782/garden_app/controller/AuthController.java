package com.haku3782.garden_app.controller;

import com.haku3782.garden_app.dto.AuthRequest;
import com.haku3782.garden_app.dto.AuthResponse;
import com.haku3782.garden_app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 認証に関する API エンドポイントを定義するコントローラークラス。
 *
 * <p>クライアント（フロントエンド等）からの HTTP リクエストを受け取り、
 * AuthService に処理を委譲して結果をレスポンスとして返す。
 * ビジネスロジックはここには書かず、Service 層に任せる。
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@RestController          : @Controller + @ResponseBody の組み合わせ。
 *                                  戻り値を自動的に JSON に変換してレスポンスに書き込む</li>
 *   <li>@RequestMapping("/api/auth") : このクラス内の全エンドポイントの共通パスプレフィックス</li>
 *   <li>@RequiredArgsConstructor : final フィールドへの依存性注入用コンストラクタを自動生成（Lombok）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 認証ビジネスロジックを担当するサービス */
    private final AuthService authService;

    /**
     * ユーザー登録エンドポイント。
     *
     * <p>エンドポイント： POST /api/auth/register
     *
     * <p>処理の流れ：
     * <ol>
     *   <li>リクエスト body の JSON を AuthRequest に自動変換（@RequestBody）</li>
     *   <li>AuthService.register() を呼び出してユーザー登録と JWT 生成を行う</li>
     *   <li>JWT を含む AuthResponse を HTTP 200 OK で返す</li>
     * </ol>
     *
     * @param request ユーザー名とパスワードを含むリクエスト body
     * @return HTTP 200 と JWT を含む AuthResponse
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * ログインエンドポイント。
     *
     * <p>エンドポイント： POST /api/auth/login
     *
     * <p>処理の流れ：
     * <ol>
     *   <li>リクエスト body の JSON を AuthRequest に自動変換（@RequestBody）</li>
     *   <li>AuthService.login() を呼び出してパスワード照合と JWT 生成を行う</li>
     *   <li>JWT を含む AuthResponse を HTTP 200 OK で返す</li>
     * </ol>
     *
     * @param request ユーザー名とパスワードを含むリクエスト body
     * @return HTTP 200 と JWT を含む AuthResponse
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
