package com.haku3782.garden_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Supabase Storage への画像アップロードを担当するサービスクラス。
 *
 * <p>Supabase StorageはREST APIとして公開されており、バケットに対して
 * {@code Authorization: Bearer <secret key>} でアップロードする。
 * このアプリは Supabase Auth を使わず独自の JWT 認証を行っているため、
 * フロントから直接アップロードはせず、必ずこのサービス（バックエンド経由）で行う。
 */
@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.secret-key}")
    private String secretKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final RestClient restClient = RestClient.create();

    /**
     * 画像ファイルをSupabase Storageにアップロードし、公開URLを返す。
     *
     * <p>ファイル名はUUIDで生成し直すため、元のファイル名や拡張子に依存しない
     * 一意なパスになる（拡張子のみ元のファイル名から引き継ぐ）。
     *
     * @param file アップロード対象の画像ファイル
     * @return アップロードされた画像の公開URL
     */
    public String upload(MultipartFile file) {
        String path = UUID.randomUUID() + extractExtension(file.getOriginalFilename());

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("ファイルの読み込みに失敗しました", e);
        }

        restClient.post()
                .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + path)
                .header("Authorization", "Bearer " + secretKey)
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(bytes)
                .retrieve()
                .toBodilessEntity();

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    /**
     * Supabase Storageから画像ファイルを削除する。
     *
     * <p>uploadが返した公開URL（{@code {supabaseUrl}/storage/v1/object/public/{bucket}/...}）
     * からパス部分を取り出して削除APIを呼び出す。URLの形式が想定と異なる場合は何もしない。
     *
     * @param photoUrl 削除対象の画像の公開URL
     */
    public void delete(String photoUrl) {
        String prefix = supabaseUrl + "/storage/v1/object/public/" + bucket + "/";
        if (photoUrl == null || !photoUrl.startsWith(prefix)) {
            return;
        }
        String path = photoUrl.substring(prefix.length());

        restClient.delete()
                .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + path)
                .header("Authorization", "Bearer " + secretKey)
                .retrieve()
                .toBodilessEntity();
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : "";
    }
}
