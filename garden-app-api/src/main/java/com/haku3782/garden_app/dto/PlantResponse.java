package com.haku3782.garden_app.dto;

import com.haku3782.garden_app.domain.PlantType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 植物 API のレスポンス body をマッピングするクラス。
 *
 * <p>GET / POST / PUT のレスポンスとして使用する。
 * Plant エンティティをそのまま返すのではなく、
 * 必要なフィールドだけを選んで返すことで API の安全性・柔軟性を高める。
 *
 * <p>【エンティティとの違い】
 * Plant エンティティには User（FetchType.LAZY）が含まれるため、
 * そのまま JSON 化するとパスワードなど不要な情報も露出してしまう。
 * PlantResponse に詰め替えることでそれを防ぐ。
 *
 * <p>@Data（Lombok）により getter / setter / equals / hashCode / toString を自動生成。
 */
@Data
public class PlantResponse {
    /** 植物の一意な識別子 */
    private UUID id;

    /** 植物の名前 */
    private String name;

    /** 植物の種別（vegetable / fruit / herb / flower / tree / other） */
    private PlantType type;

    /** 植えた日時 */
    private LocalDateTime plantedAt;

    /** メモ */
    private String memo;

    /** 最新の写真付きケア記録の写真URL（無ければ null） */
    private String latestPhotoUrl;

    /** レコードの作成日時 */
    private LocalDateTime createdAt;
}
