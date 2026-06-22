package com.haku3782.garden_app.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 全植物横断の写真ギャラリー API のレスポンス body をマッピングするクラス。
 *
 * <p>CareLogResponse とは異なり、どの植物の写真かを画面上で識別できるよう
 * plantName を含む。
 *
 * <p>@Data（Lombok）により getter / setter / equals / hashCode / toString を自動生成。
 */
@Data
public class PhotoGalleryItemResponse {
    /** ケアログの一意な識別子 */
    private UUID id;

    /** このケアログが紐づく植物の ID */
    private UUID plantId;

    /** このケアログが紐づく植物の名前（ギャラリー表示用） */
    private String plantName;

    /** ケアの種類 */
    private String careType;

    /** ケアを行った日時 */
    private LocalDateTime caredAt;

    /** 写真のURL */
    private String photoUrl;

    /** メモ */
    private String memo;
}
