package com.haku3782.garden_app.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * plants テーブルに対応するエンティティクラス。
 *
 * <p>ユーザーが登録した植物の情報を表す。
 * User エンティティと多対一の関係を持つ（1ユーザーが複数の植物を持てる）。
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@Entity          : JPA の管理対象エンティティとして登録</li>
 *   <li>@Table           : 対応する DB テーブル名を "plants" に指定</li>
 *   <li>@Data            : getter / setter / equals / hashCode / toString を自動生成（Lombok）</li>
 * </ul>
 */
@Data
@Entity
@Table(name = "plants")
public class Plant {

    /**
     * 植物の一意な識別子（主キー）。
     * UUID を DB に自動生成させる。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * この植物を登録したユーザー（外部キー：user_id）。
     *
     * <p>@ManyToOne : 多くの植物が1人のユーザーに属する関係。
     * FetchType.LAZY : Plant を取得しても User を即座に JOIN せず、
     * 必要になった時点で DB へ問い合わせる（パフォーマンス最適化）。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 植物の名前。必須項目（nullable = false）。
     */
    @Column(nullable = false)
    private String name;

    /**
     * 植物の種別（vegetable / fruit / herb / flower / tree / other）。
     *
     * <p>@Enumerated(EnumType.STRING) : DB に数値ではなく文字列で保存する。
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM) : PostgreSQL のネイティブ ENUM 型（plant_type）に
     * 正しくキャストして送るよう Hibernate に指示する。
     * これがないと Hibernate が character varying で送り、
     * "column type is plant_type but expression is character varying" エラーが発生する。
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", columnDefinition = "plant_type")
    private PlantType type;

    /**
     * 植えた日時。任意項目。日付だけでなく時刻も記録できる。
     */
    @Column(name = "planted_at")
    private LocalDateTime plantedAt;

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
