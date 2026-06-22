package com.haku3782.garden_app.controller;

import com.haku3782.garden_app.dto.PhotoGalleryItemResponse;
import com.haku3782.garden_app.service.CareLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 全植物横断の写真ギャラリーに関する API エンドポイントを定義するコントローラークラス。
 *
 * <p>全エンドポイントは JWT 認証が必要（SecurityConfig で /api/** を認証必須に設定済み）。
 * ビジネスロジックは CareLogService に委譲する。
 *
 * <p>【エンドポイント一覧】
 * <ul>
 *   <li>GET /api/gallery/photos : ログイン中のユーザーが所有する全植物の写真付きケアログを取得</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryController {

    /** ケアログのビジネスロジックを担当するサービス */
    private final CareLogService careLogService;

    /**
     * ログイン中のユーザーが所有する全植物の写真付きケアログを、ケア日付の降順で取得する。
     *
     * <p>エンドポイント： GET /api/gallery/photos
     *
     * @return HTTP 200 と写真付きケアログのリスト（新しい順）
     */
    @GetMapping("/photos")
    public ResponseEntity<List<PhotoGalleryItemResponse>> getPhotos() {
        return ResponseEntity.ok(careLogService.getPhotoGallery());
    }
}
