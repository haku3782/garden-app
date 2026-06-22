package com.haku3782.garden_app.service;

import com.haku3782.garden_app.domain.CareLog;
import com.haku3782.garden_app.domain.Plant;
import com.haku3782.garden_app.dto.CareLogRequest;
import com.haku3782.garden_app.dto.CareLogResponse;
import com.haku3782.garden_app.dto.PhotoGalleryItemResponse;
import com.haku3782.garden_app.repository.CareLogRepository;
import com.haku3782.garden_app.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ケアログ（CareLog）に関するビジネスロジックを担当するサービスクラス。
 *
 * <p>CareLogController から呼び出され、特定の植物に紐づくケアログの
 * 取得・登録・削除を行う。
 *
 * <p>【主なアノテーション】
 * <ul>
 *   <li>@Service               : Spring のサービス層コンポーネントとして登録</li>
 *   <li>@RequiredArgsConstructor : final フィールドへの依存性注入用コンストラクタを自動生成（Lombok）</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CareLogService {

    /** care_logs テーブルへのデータアクセスを担うリポジトリ */
    private final CareLogRepository careLogRepository;

    /** plants テーブルへのデータアクセスを担うリポジトリ（植物の存在確認に使用） */
    private final PlantRepository plantRepository;

    /** Supabase Storage への画像アップロードを担うサービス */
    private final SupabaseStorageService supabaseStorageService;

    /** アップロードを許可する画像ファイルのMIMEタイプ */
    private static final Set<String> ALLOWED_PHOTO_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    /** アップロードを許可する画像ファイルの最大サイズ（5MB） */
    private static final long MAX_PHOTO_SIZE_BYTES = 5L * 1024 * 1024;

    /**
     * SecurityContext からログイン中のユーザー名を取得する。
     *
     * @return ログイン中のユーザー名
     */
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    /**
     * 指定した植物がログイン中のユーザーの所有物であることを確認する。
     *
     * @param plant 確認対象の植物
     * @throws AccessDeniedException ログイン中のユーザーが所有者でない場合
     */
    private void requireOwner(Plant plant) {
        if (!plant.getUser().getUsername().equals(getCurrentUsername())) {
            throw new AccessDeniedException("この植物のケアログを操作する権限がありません");
        }
    }

    /**
     * CareLog エンティティを CareLogResponse DTO に変換する。
     *
     * <p>エンティティをそのまま返すと Plant（パスワード等を含む User）が露出するため、
     * 必要なフィールドだけを CareLogResponse に詰め替えてから返す。
     * plant_id は Plant オブジェクトから UUID のみ取り出して返す。
     *
     * @param careLog 変換元の CareLog エンティティ
     * @return クライアントに返す CareLogResponse
     */
    private CareLogResponse toResponse(CareLog careLog) {
        CareLogResponse res = new CareLogResponse();
        res.setId(careLog.getId());
        res.setPlantId(careLog.getPlant().getId());
        res.setCareType(careLog.getCareType());
        res.setCaredAt(careLog.getCaredAt());
        res.setMemo(careLog.getMemo());
        res.setPhotoUrl(careLog.getPhotoUrl());
        res.setCreatedAt(careLog.getCreatedAt());
        return res;
    }

    /**
     * CareLog エンティティを PhotoGalleryItemResponse DTO に変換する（写真ギャラリー表示用）。
     *
     * @param careLog 変換元の CareLog エンティティ
     * @return クライアントに返す PhotoGalleryItemResponse
     */
    private PhotoGalleryItemResponse toGalleryItem(CareLog careLog) {
        PhotoGalleryItemResponse res = new PhotoGalleryItemResponse();
        res.setId(careLog.getId());
        res.setPlantId(careLog.getPlant().getId());
        res.setPlantName(careLog.getPlant().getName());
        res.setCareType(careLog.getCareType());
        res.setCaredAt(careLog.getCaredAt());
        res.setPhotoUrl(careLog.getPhotoUrl());
        res.setMemo(careLog.getMemo());
        return res;
    }

    /**
     * 指定した植物 ID に紐づくケアログをケア日付の降順で取得する。
     *
     * <p>最新のケアが先頭に来るよう降順（DESC）で返す。
     * ログイン中のユーザーが所有する植物でない場合はアクセスを拒否する。
     *
     * @param plantId 対象の植物 ID
     * @return ケアログのリスト（新しい順）
     * @throws RuntimeException      指定 ID の植物が存在しない場合
     * @throws AccessDeniedException ログイン中のユーザーが所有者でない場合
     */
    public List<CareLogResponse> getByPlantId(UUID plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("植物が見つかりません"));
        requireOwner(plant);
        return careLogRepository.findByPlantIdOrderByCaredAtDesc(plantId)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 指定した植物にケアログを新規登録する。
     *
     * <p>処理の流れ：
     * <ol>
     *   <li>植物 ID で DB を検索し、存在しない場合は例外をスロー</li>
     *   <li>ログイン中のユーザーが所有する植物でない場合はアクセスを拒否</li>
     *   <li>CareLog エンティティを生成して plants と紐づけて保存</li>
     *   <li>保存結果を CareLogResponse に変換して返す</li>
     * </ol>
     *
     * @param plantId 対象の植物 ID
     * @param request ケアの種類・日付・メモを含むリクエスト
     * @return 登録したケアログの CareLogResponse
     * @throws RuntimeException      指定 ID の植物が存在しない場合
     * @throws AccessDeniedException ログイン中のユーザーが所有者でない場合
     */
    public CareLogResponse create(UUID plantId, CareLogRequest request) {
        // 植物の存在確認（存在しない場合は例外）
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("植物が見つかりません"));
        requireOwner(plant);
        CareLog careLog = new CareLog();
        careLog.setPlant(plant);
        careLog.setCareType(request.getCareType());
        careLog.setCaredAt(request.getCaredAt());
        careLog.setMemo(request.getMemo());
        return toResponse(careLogRepository.save(careLog));
    }

    /**
     * 指定した ID のケアログを削除する。
     *
     * <p>URL 上の plantId と紐付け先の植物が一致しない場合、および
     * ログイン中のユーザーが所有する植物でない場合はアクセスを拒否する。
     *
     * @param plantId URL パス上の植物 ID（ケアログの紐付け先と一致するか確認するために使用）
     * @param id      削除対象のケアログ ID
     * @throws RuntimeException      指定 ID のケアログが存在しない場合、または plantId と紐付け先が一致しない場合
     * @throws AccessDeniedException ログイン中のユーザーが所有者でない場合
     */
    public void delete(UUID plantId, UUID id) {
        CareLog careLog = careLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ケアログが見つかりません"));
        if (!careLog.getPlant().getId().equals(plantId)) {
            throw new RuntimeException("指定された植物に紐づくケアログではありません");
        }
        requireOwner(careLog.getPlant());
        careLogRepository.deleteById(id);
    }

    /**
     * 指定したケアログに写真を添付する。
     *
     * <p>処理の流れ：
     * <ol>
     *   <li>ケアログ ID で DB を検索し、存在しない場合は例外をスロー</li>
     *   <li>URL 上の plantId とケアログの紐付け先が一致するか確認</li>
     *   <li>ログイン中のユーザーが所有する植物でない場合はアクセスを拒否</li>
     *   <li>ファイルサイズ・MIMEタイプを検証</li>
     *   <li>Supabase Storage にアップロードし、公開URLを CareLog に保存</li>
     * </ol>
     *
     * <p>1件のケア記録に登録できる写真は1枚のみ。再アップロードすると上書きされる。
     *
     * @param plantId URL パス上の植物 ID（ケアログの紐付け先と一致するか確認するために使用）
     * @param id      写真を添付するケアログ ID
     * @param photo   アップロードする画像ファイル（JPEG/PNG/WebP、5MBまで）
     * @return 更新後のケアログの CareLogResponse
     * @throws RuntimeException        指定 ID のケアログが存在しない場合、または plantId と紐付け先が一致しない場合
     * @throws AccessDeniedException   ログイン中のユーザーが所有者でない場合
     * @throws IllegalArgumentException ファイルが空・サイズ超過・対応していない形式の場合
     */
    public CareLogResponse uploadPhoto(UUID plantId, UUID id, MultipartFile photo) {
        CareLog careLog = careLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ケアログが見つかりません"));
        if (!careLog.getPlant().getId().equals(plantId)) {
            throw new RuntimeException("指定された植物に紐づくケアログではありません");
        }
        requireOwner(careLog.getPlant());

        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("ファイルが指定されていません");
        }
        if (photo.getSize() > MAX_PHOTO_SIZE_BYTES) {
            throw new IllegalArgumentException("ファイルサイズは5MBまでです");
        }
        if (!ALLOWED_PHOTO_CONTENT_TYPES.contains(photo.getContentType())) {
            throw new IllegalArgumentException("対応していないファイル形式です（JPEG/PNG/WebPのみ）");
        }

        String photoUrl = supabaseStorageService.upload(photo);
        careLog.setPhotoUrl(photoUrl);
        return toResponse(careLogRepository.save(careLog));
    }

    /**
     * ログイン中のユーザーが所有する植物のうち、写真が登録されているケアログを
     * ケア日付の降順で取得する（全植物横断の写真ギャラリー表示用）。
     *
     * @return 写真付きケアログのリスト（新しいケア日順）
     */
    public List<PhotoGalleryItemResponse> getPhotoGallery() {
        return careLogRepository.findByPlant_User_UsernameAndPhotoUrlIsNotNullOrderByCaredAtDesc(getCurrentUsername())
                .stream().map(this::toGalleryItem)
                .collect(Collectors.toList());
    }
}
