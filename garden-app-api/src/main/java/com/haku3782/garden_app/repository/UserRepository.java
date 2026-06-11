package com.haku3782.garden_app.repository;

import com.haku3782.garden_app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * users テーブルへのデータアクセスを担当するリポジトリインターフェース。
 *
 * <p>JpaRepository を継承することで、以下の基本的な CRUD 操作が
 * 実装不要で自動的に使えるようになる。
 * <ul>
 *   <li>save(entity)       : INSERT / UPDATE</li>
 *   <li>findById(id)       : 主キーで1件取得</li>
 *   <li>findAll()          : 全件取得</li>
 *   <li>deleteById(id)     : 主キーで削除</li>
 * </ul>
 *
 * <p>【型パラメータ】
 * <ul>
 *   <li>第1引数 User : 操作対象のエンティティ</li>
 *   <li>第2引数 UUID : エンティティの主キーの型</li>
 * </ul>
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * ユーザー名でユーザーを検索する。
     *
     * <p>Spring Data JPA がメソッド名から自動的に SQL を生成するため、
     * 実装コードは不要。
     * 生成される SQL のイメージ：
     * {@code SELECT * FROM users WHERE username = ?}
     *
     * <p>ユーザー名が存在しない場合は空の Optional を返すため、
     * 呼び出し側で null チェックが不要になる。
     *
     * @param username 検索するユーザー名
     * @return 該当ユーザーを含む Optional。存在しない場合は Optional.empty()
     */
    Optional<User> findByUsername(String username);
}
