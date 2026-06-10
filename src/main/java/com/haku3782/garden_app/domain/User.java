package com.haku3782.garden_app.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * users テーブルに対応するエンティティクラス。
 *
 * <p>アプリケーションに登録されたユーザーの情報を表す。
 * JPA によって DB のレコードと自動的にマッピングされる。
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@Entity : このクラスが JPA の管理対象エンティティであることを示す</li>
 *   <li>@Table  : 対応する DB テーブル名を "users" に指定</li>
 *   <li>@Data   : getter / setter / equals / hashCode / toString を自動生成（Lombok）</li>
 * </ul>
 */
@Data
@Entity
@Table(name = "users")
public class User {

    /**
     * ユーザーの一意な識別子（主キー）。
     * UUID を DB に自動生成させる。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ユーザー名。
     * 重複不可（unique = true）、かつ必須項目（nullable = false）。
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * パスワード（ハッシュ化済みの文字列を保存する）。
     * 必須項目（nullable = false）。
     */
    @Column(nullable = false)
    private String password;

    /**
     * レコードの作成日時。
     * DB への INSERT 直前に prePersist() によって自動セットされる。
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * DB への INSERT 直前に自動実行されるコールバックメソッド。
     *
     * <p>createdAt フィールドに現在日時をセットすることで、
     * 呼び出し側が明示的に日時を指定しなくても自動的に記録される。
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
