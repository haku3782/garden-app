package com.haku3782.garden_app.repository;

import com.haku3782.garden_app.domain.CareLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * care_logs テーブルへのデータアクセスを担当するリポジトリインターフェース。
 *
 * <p>JpaRepository を継承することで、基本的な CRUD 操作が実装不要で使える。
 *
 * <p>【型パラメータ】
 * <ul>
 *   <li>第1引数 CareLog : 操作対象のエンティティ</li>
 *   <li>第2引数 UUID    : エンティティの主キーの型</li>
 * </ul>
 */
public interface CareLogRepository extends JpaRepository<CareLog, UUID> {

    /**
     * 植物 ID に紐づくケアログを取得する（順序なし）。
     *
     * <p>生成される SQL のイメージ：
     * {@code SELECT * FROM care_logs WHERE plant_id = ?}
     *
     * @param plantId 対象の植物 ID
     * @return 該当するケアログのリスト
     */
    List<CareLog> findByPlantId(UUID plantId);

    /**
     * 植物 ID に紐づくケアログをケア日付の降順で取得する。
     *
     * <p>最新のケアが先頭に来るよう DESC（降順）で返す。
     * CareLogService.getByPlantId() から呼び出される。
     *
     * <p>生成される SQL のイメージ：
     * {@code SELECT * FROM care_logs WHERE plant_id = ? ORDER BY cared_at DESC}
     *
     * @param plantId 対象の植物 ID
     * @return 該当するケアログのリスト（新しいケア日順）
     */
    List<CareLog> findByPlantIdOrderByCaredAtDesc(UUID plantId);

    /**
     * 指定したユーザーが所有する植物のうち、写真が登録されているケアログを
     * ケア日付の降順で取得する（全植物横断の写真ギャラリー表示用）。
     *
     * <p>生成される SQL のイメージ：
     * {@code SELECT cl.* FROM care_logs cl JOIN plants p ON cl.plant_id = p.id
     *         JOIN users u ON p.user_id = u.id
     *         WHERE u.username = ? AND cl.photo_url IS NOT NULL ORDER BY cl.cared_at DESC}
     *
     * @param username 対象ユーザーのユーザー名
     * @return 写真付きケアログのリスト（新しいケア日順）
     */
    List<CareLog> findByPlant_User_UsernameAndPhotoUrlIsNotNullOrderByCaredAtDesc(String username);
}
