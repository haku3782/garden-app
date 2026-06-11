package com.haku3782.garden_app.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * ケアログの登録 API へのリクエスト body をマッピングするクラス。
 *
 * <p>POST /api/plants/{plantId}/care-logs を呼び出す際に
 * JSON として送信するケア情報を受け取る。
 *
 * <p>@Data（Lombok）により getter / setter / equals / hashCode / toString を自動生成。
 */
@Data
public class CareLogRequest {
    /** ケアの種類（例：水やり、施肥、剪定 など自由記述） */
    private String careType;

    /** ケアを行った日時。例: "2026-06-11T09:00:00" */
    private LocalDateTime caredAt;

    /** メモ（任意・自由記述） */
    private String memo;
}
