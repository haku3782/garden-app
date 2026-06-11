package com.haku3782.garden_app.repository;

import com.haku3782.garden_app.domain.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * plants テーブルへのデータアクセスを担当するリポジトリインターフェース。
 *
 * <p>JpaRepository を継承することで、基本的な CRUD 操作が実装不要で使える。
 *
 * <p>【型パラメータ】
 * <ul>
 *   <li>第1引数 Plant : 操作対象のエンティティ</li>
 *   <li>第2引数 UUID  : エンティティの主キーの型</li>
 * </ul>
 */
public interface PlantRepository extends JpaRepository<Plant, UUID> {

    /**
     * ユーザー名に紐づく植物を全件取得する。
     *
     * <p>Spring Data JPA がメソッド名からSQLを自動生成する。
     * 生成される SQL のイメージ：
     * {@code SELECT p.* FROM plants p JOIN users u ON p.user_id = u.id WHERE u.username = ?}
     *
     * <p>「findBy + User（リレーション名） + Username（User のフィールド名）」
     * という命名規則でネストしたフィールドも横断検索できる。
     *
     * @param username 検索するユーザー名
     * @return そのユーザーに紐づく植物のリスト（0件の場合は空リスト）
     */
    List<Plant> findByUserUsername(String username);
}
