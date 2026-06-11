package com.haku3782.garden_app.controller;

import com.haku3782.garden_app.dto.CareLogRequest;
import com.haku3782.garden_app.dto.CareLogResponse;
import com.haku3782.garden_app.service.CareLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ケアログに関する API エンドポイントを定義するコントローラークラス。
 *
 * <p>ケアログは特定の植物に紐づくリソースのため、
 * URL に植物 ID を含むネスト構造になっている。
 *
 * <p>全エンドポイントは JWT 認証が必要（SecurityConfig で認証必須に設定済み）。
 * ビジネスロジックは CareLogService に委譲する。
 *
 * <p>【エンドポイント一覧】
 * <ul>
 *   <li>GET    /api/plants/{plantId}/care-logs       : 指定植物のケアログを全件取得</li>
 *   <li>POST   /api/plants/{plantId}/care-logs       : ケアログを新規登録</li>
 *   <li>DELETE /api/plants/{plantId}/care-logs/{id}  : ケアログを削除</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/plants/{plantId}/care-logs")
@RequiredArgsConstructor
public class CareLogController {

    /** ケアログのビジネスロジックを担当するサービス */
    private final CareLogService careLogService;

    /**
     * 指定した植物のケアログを全件取得する（新しい順）。
     *
     * <p>エンドポイント： GET /api/plants/{plantId}/care-logs
     *
     * @param plantId パスパラメータの植物 ID
     * @return HTTP 200 とケアログリスト（新しいケア日順）
     */
    @GetMapping
    public ResponseEntity<List<CareLogResponse>> getAll(@PathVariable UUID plantId) {
        return ResponseEntity.ok(careLogService.getByPlantId(plantId));
    }

    /**
     * 指定した植物にケアログを新規登録する。
     *
     * <p>エンドポイント： POST /api/plants/{plantId}/care-logs
     *
     * @param plantId パスパラメータの植物 ID
     * @param request ケアの種類・日付・メモを含むリクエスト body
     * @return HTTP 200 と登録したケアログの情報
     */
    @PostMapping
    public ResponseEntity<CareLogResponse> create(@PathVariable UUID plantId,
                                                   @RequestBody CareLogRequest request) {
        return ResponseEntity.ok(careLogService.create(plantId, request));
    }

    /**
     * 指定した ID のケアログを削除する。
     *
     * <p>エンドポイント： DELETE /api/plants/{plantId}/care-logs/{id}
     * plantId はパス上に必要だが、削除処理では id のみを使用する。
     *
     * @param plantId パスパラメータの植物 ID（パス構造のために必要）
     * @param id      削除対象のケアログ ID
     * @return HTTP 204 No Content（ボディなし）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID plantId,
                                        @PathVariable UUID id) {
        careLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
