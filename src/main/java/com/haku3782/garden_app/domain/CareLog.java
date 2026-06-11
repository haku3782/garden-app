package com.haku3782.garden_app.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * care_logs テーブルに対応するエンティティクラス。
 *
 * <p>植物に対して行ったケア（水やり・施肥・剪定など）の記録を表す。
 * Plant エンティティと多対一の関係を持つ（1つの植物に複数のケアログが紐づく）。
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@Entity : JPA の管理対象エンティティとして登録</li>
 *   <li>@Table  : 対応する DB テーブル名を "care_logs" に指定</li>
 *   <li>@Data   : getter / setter / equals / hashCode / toString を自動生成（Lombok）</li>
 * </ul>
 */
@Data
@Entity
@Table(name = "care_logs")
public class CareLog {

    /**
     * ケアログの一意な識別子（主キー）。
     * UUID を DB に自動生成させる。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * このケアログが紐づく植物（外部キー：plant_id）。
     *
     * <p>@ManyToOne : 多くのケアログが1つの植物に属する関係。
     * FetchType.LAZY : CareLog を取得しても Plant を即座に JOIN せず、
     * 必要になった時点で DB へ問い合わせる（パフォーマンス最適化）。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    /**
     * ケアの種類（例：水やり、施肥、剪定など）。
     * 自由記述のため Enum ではなく String で持つ。必須項目（nullable = false）。
     */
    @Column(name = "care_type", nullable = false)
    private String careType;

    /**
     * ケアを行った日時。必須項目（nullable = false）。
     */
    @Column(name = "cared_at", nullable = false)
    private LocalDateTime caredAt;

    /**
     * メモ。任意項目。自由記述欄。
     */
    private String memo;

    /**
     * レコードの作成日時。
     * DB への INSERT 直前に prePersist() によって自動セットされる。
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * DB への INSERT 直前に自動実行されるコールバックメソッド。
     * createdAt フィールドに現在日時をセットする。
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
