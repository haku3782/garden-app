package com.haku3782.garden_app.service;

import com.haku3782.garden_app.domain.Plant;
import com.haku3782.garden_app.domain.User;
import com.haku3782.garden_app.dto.PlantRequest;
import com.haku3782.garden_app.dto.PlantResponse;
import com.haku3782.garden_app.repository.PlantRepository;
import com.haku3782.garden_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 植物（Plant）に関するビジネスロジックを担当するサービスクラス。
 *
 * <p>PlantController から呼び出され、CRUD 操作を行う。
 * 操作は常にログイン中のユーザーに紐づいて実行される。
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@Service               : Spring のサービス層コンポーネントとして登録</li>
 *   <li>@RequiredArgsConstructor : final フィールドへの依存性注入用コンストラクタを自動生成（Lombok）</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PlantService {

    /** plants テーブルへのデータアクセスを担うリポジトリ */
    private final PlantRepository plantRepository;

    /** users テーブルへのデータアクセスを担うリポジトリ */
    private final UserRepository userRepository;

    /**
     * SecurityContext からログイン中のユーザー名を取得する。
     *
     * <p>JwtFilter が SecurityContext にセットした認証情報から名前を取り出す。
     * 認証済みリクエストでのみ有効な値が返る。
     *
     * @return ログイン中のユーザー名
     */
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    /**
     * ログイン中のユーザーを DB から取得する。
     *
     * @return ログイン中の User エンティティ
     * @throws RuntimeException ユーザーが DB に存在しない場合（通常は起こらない）
     */
    private User getCurrentUser() {
        return userRepository.findByUsername(getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
    }

    /**
     * Plant エンティティを PlantResponse DTO に変換する。
     *
     * <p>エンティティをそのまま返すと User（パスワード等）が露出するため、
     * 必要なフィールドだけを PlantResponse に詰め替えてから返す。
     *
     * @param plant 変換元の Plant エンティティ
     * @return クライアントに返す PlantResponse
     */
    private PlantResponse toResponse(Plant plant) {
        PlantResponse res = new PlantResponse();
        res.setId(plant.getId());
        res.setName(plant.getName());
        res.setType(plant.getType());
        res.setPlantedAt(plant.getPlantedAt());
        res.setMemo(plant.getMemo());
        res.setCreatedAt(plant.getCreatedAt());
        return res;
    }

    /**
     * ログイン中のユーザーの植物を全件取得する。
     *
     * @return ログイン中ユーザーの植物リスト（0件の場合は空リスト）
     */
    public List<PlantResponse> getAll() {
        return plantRepository.findByUserUsername(getCurrentUsername())
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 新しい植物を登録する。
     *
     * <p>ログイン中のユーザーに紐づけて plants テーブルに INSERT する。
     *
     * @param request 植物の情報（名前・種別・植えた日・メモ）
     * @return 登録した植物の PlantResponse
     */
    public PlantResponse create(PlantRequest request) {
        Plant plant = new Plant();
        plant.setUser(getCurrentUser());
        plant.setName(request.getName());
        plant.setType(request.getType());
        plant.setPlantedAt(request.getPlantedAt());
        plant.setMemo(request.getMemo());
        return toResponse(plantRepository.save(plant));
    }

    /**
     * 指定した ID の植物を更新する。
     *
     * @param id      更新対象の植物 ID
     * @param request 更新後の情報（名前・種別・植えた日・メモ）
     * @return 更新後の植物の PlantResponse
     * @throws RuntimeException 指定 ID の植物が存在しない場合
     */
    public PlantResponse update(UUID id, PlantRequest request) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("植物が見つかりません"));
        plant.setName(request.getName());
        plant.setType(request.getType());
        plant.setPlantedAt(request.getPlantedAt());
        plant.setMemo(request.getMemo());
        return toResponse(plantRepository.save(plant));
    }

    /**
     * 指定した ID の植物を削除する。
     *
     * @param id 削除対象の植物 ID
     */
    public void delete(UUID id) {
        plantRepository.deleteById(id);
    }
}
