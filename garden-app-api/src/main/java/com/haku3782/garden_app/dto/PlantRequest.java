package com.haku3782.garden_app.dto;

import com.haku3782.garden_app.domain.PlantType;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 植物の登録・更新 API へのリクエスト body をマッピングするクラス。
 *
 * <p>POST /api/plants（登録）および PUT /api/plants/{id}（更新）の
 * リクエスト body として使用する。
 *
 * <p>@Data（Lombok）により getter / setter / equals / hashCode / toString を自動生成。
 */
@Data
public class PlantRequest {
    /** 植物の名前 */
    private String name;

    /** 植物の種別（vegetable / fruit / herb / flower / tree / houseplant / succulent / airplant / other） */
    private PlantType type;

    /** 植えた日時（任意）。例: "2026-06-11T09:00:00" */
    private LocalDateTime plantedAt;

    /** メモ（任意・自由記述） */
    private String memo;
}
