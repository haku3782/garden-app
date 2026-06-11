package com.haku3782.garden_app.service;

import com.haku3782.garden_app.domain.CareLog;
import com.haku3782.garden_app.domain.Plant;
import com.haku3782.garden_app.dto.CareLogRequest;
import com.haku3782.garden_app.dto.CareLogResponse;
import com.haku3782.garden_app.repository.CareLogRepository;
import com.haku3782.garden_app.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ケアログ（CareLog）に関するビジネスロジックを担当するサービスクラス。
 *
 * <p>CareLogController から呼び出され、特定の植物に紐づくケアログの
 * 取得・登録・削除を行う。
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@Service               : Spring のサービス層コンポーネントとして登録</li>
 *   <li>@RequiredArgsConstructor : final フィールドへの依存性注入用コンストラクタを自動生成（Lombok）</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CareLogService {

    /** care_logs テーブルへのデータアクセスを担うリポジトリ */
    private final CareLogRepository careLogRepository;

    /** plants テーブルへのデータアクセスを担うリポジトリ（植物の存在確認に使用） */
    private final PlantRepository plantRepository;

    /**
     * CareLog エンティティを CareLogResponse DTO に変換する。
     *
     * <p>エンティティをそのまま返すと Plant（パスワード等を含む User）が露出するため、
     * 必要なフィールドだけを CareLogResponse に詰め替えてから返す。
     * plant_id は Plant オブジェクトから UUID のみ取り出して返す。
     *
     * @param careLog 変換元の CareLog エンティティ
     * @return クライアントに返す CareLogResponse
     */
    private CareLogResponse toResponse(CareLog careLog) {
        CareLogResponse res = new CareLogResponse();
        res.setId(careLog.getId());
        res.setPlantId(careLog.getPlant().getId());
        res.setCareType(careLog.getCareType());
        res.setCaredAt(careLog.getCaredAt());
        res.setMemo(careLog.getMemo());
        res.setCreatedAt(careLog.getCreatedAt());
        return res;
    }

    /**
     * 指定した植物 ID に紐づくケアログをケア日付の降順で取得する。
     *
     * <p>最新のケアが先頭に来るよう降順（DESC）で返す。
     *
     * @param plantId 対象の植物 ID
     * @return ケアログのリスト（新しい順）
     */
    public List<CareLogResponse> getByPlantId(UUID plantId) {
        return careLogRepository.findByPlantIdOrderByCaredAtDesc(plantId)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 指定した植物にケアログを新規登録する。
     *
     * <p>処理の流れ：
     * <ol>
     *   <li>植物 ID で DB を検索し、存在しない場合は例外をスロー</li>
     *   <li>CareLog エンティティを生成して plants と紐づけて保存</li>
     *   <li>保存結果を CareLogResponse に変換して返す</li>
     * </ol>
     *
     * @param plantId 対象の植物 ID
     * @param request ケアの種類・日付・メモを含むリクエスト
     * @return 登録したケアログの CareLogResponse
     * @throws RuntimeException 指定 ID の植物が存在しない場合
     */
    public CareLogResponse create(UUID plantId, CareLogRequest request) {
        // 植物の存在確認（存在しない場合は例外）
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("植物が見つかりません"));
        CareLog careLog = new CareLog();
        careLog.setPlant(plant);
        careLog.setCareType(request.getCareType());
        careLog.setCaredAt(request.getCaredAt());
        careLog.setMemo(request.getMemo());
        return toResponse(careLogRepository.save(careLog));
    }

    /**
     * 指定した ID のケアログを削除する。
     *
     * @param id 削除対象のケアログ ID
     */
    public void delete(UUID id) {
        careLogRepository.deleteById(id);
    }
}
