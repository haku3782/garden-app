package com.haku3782.garden_app.controller;

import com.haku3782.garden_app.dto.PlantRequest;
import com.haku3782.garden_app.dto.PlantResponse;
import com.haku3782.garden_app.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 植物（Plant）に関する CRUD API エンドポイントを定義するコントローラークラス。
 *
 * <p>全エンドポイントは JWT 認証が必要（SecurityConfig で /api/plants/** を認証必須に設定済み）。
 * ビジネスロジックは PlantService に委譲する。
 *
 * <p>【エンドポイント一覧】
 * <ul>
 *   <li>GET    /api/plants       : 自分の植物を全件取得</li>
 *   <li>POST   /api/plants       : 植物を新規登録</li>
 *   <li>PUT    /api/plants/{id}  : 指定した植物を更新</li>
 *   <li>DELETE /api/plants/{id}  : 指定した植物を削除</li>
 * </ul>
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@RestController          : 戻り値を自動的に JSON に変換してレスポンスに書き込む</li>
 *   <li>@RequestMapping          : このクラス内の全エンドポイントの共通パスプレフィックス</li>
 *   <li>@RequiredArgsConstructor : final フィールドへの依存性注入用コンストラクタを自動生成（Lombok）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/plants")
@RequiredArgsConstructor
public class PlantController {

    /** 植物のビジネスロジックを担当するサービス */
    private final PlantService plantService;

    /**
     * ログイン中のユーザーの植物を全件取得する。
     *
     * <p>エンドポイント： GET /api/plants
     *
     * @return HTTP 200 と植物リスト
     */
    @GetMapping
    public ResponseEntity<List<PlantResponse>> getAll() {
        return ResponseEntity.ok(plantService.getAll());
    }

    /**
     * 新しい植物を登録する。
     *
     * <p>エンドポイント： POST /api/plants
     *
     * @param request 植物の情報（名前・種別・植えた日・メモ）
     * @return HTTP 200 と登録した植物の情報
     */
    @PostMapping
    public ResponseEntity<PlantResponse> create(@RequestBody PlantRequest request) {
        return ResponseEntity.ok(plantService.create(request));
    }

    /**
     * 指定した ID の植物を更新する。
     *
     * <p>エンドポイント： PUT /api/plants/{id}
     *
     * @param id      更新対象の植物 ID（パスパラメータ）
     * @param request 更新後の植物情報
     * @return HTTP 200 と更新後の植物の情報
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlantResponse> update(@PathVariable UUID id,
                                                 @RequestBody PlantRequest request) {
        return ResponseEntity.ok(plantService.update(id, request));
    }

    /**
     * 指定した ID の植物を削除する。
     *
     * <p>エンドポイント： DELETE /api/plants/{id}
     *
     * <p>【修正】元のコードの plantService.deleteById(id) は存在しないメソッドのため
     * plantService.delete(id) に修正済み。
     *
     * @param id 削除対象の植物 ID（パスパラメータ）
     * @return HTTP 204 No Content（ボディなし）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        plantService.delete(id);  // ← deleteById → delete に修正
        return ResponseEntity.noContent().build();
    }
}
