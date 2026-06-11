package com.haku3782.garden_app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI（OpenAPI）の設定クラス。
 *
 * <p>@SecurityScheme で「Authorize」ボタンに JWT Bearer トークンを
 * 入力できるようにする。一度入力すると、以降の全リクエストに
 * Authorization: Bearer {token} ヘッダーが自動付与される。
 *
 * <p>【使い方】
 * <ol>
 *   <li>POST /api/auth/login でトークンを取得</li>
 *   <li>Swagger UI 右上の「Authorize」ボタンをクリック</li>
 *   <li>取得したトークン文字列を入力して「Authorize」</li>
 *   <li>以降の API 操作に JWT が自動付与される</li>
 * </ol>
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Garden App API",
        version = "1.0",
        description = "植物管理アプリの REST API ドキュメント"
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT トークンを入力してください（Bearer プレフィックス不要）"
)
public class SwaggerConfig {
}
