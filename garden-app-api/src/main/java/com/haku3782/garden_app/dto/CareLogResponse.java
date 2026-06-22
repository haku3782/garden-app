package com.haku3782.garden_app.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ケアログ API のレスポンス body をマッピングするクラス。
 *
 * <p>GET / POST のレスポンスとして使用する。
 * CareLog エンティティをそのまま返すと Plant（FetchType.LAZY）が露出するため、
 * 必要なフィールドだけを詰め替えて返す。
 *
 * <p>plant_id を UUID で返すことで、クライアントがどの植物のログかを
 * エンティティ全体を取得せずに把握できる。
 *
 * <p>@Data（Lombok）により getter / setter / equals / hashCode / toString を自動生成。
 */
@Data
public class CareLogResponse {
    /** ケアログの一意な識別子 */
    private UUID id;

    /** このケアログが紐づく植物の ID */
    private UUID plantId;

    /** ケアの種類 */
    private String careType;

    /** ケアを行った日時 */
    private LocalDateTime caredAt;

    /** メモ */
    private String memo;

    /** ケア記録に添付された写真のURL（無ければ null） */
    private String photoUrl;

    /** レコードの作成日時 */
    private LocalDateTime createdAt;
}
