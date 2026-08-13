package ru.anyforms.edu.service.s3;

import org.springframework.web.multipart.MultipartFile;

public interface S3FileStorage {

    /** Загружает файл в бакет, возвращает ключ объекта. */
    String upload(MultipartFile file, String keyPrefix);

    void delete(String key);

    /** Временная подписанная ссылка на объект. */
    String presignedUrl(String key);

    /**
     * imageUrl/videoUrl в БД может быть и полным URL, и ключом S3:
     * http(s)-ссылки отдаём как есть, ключи превращаем в presigned URL.
     */
    default String resolveUrl(String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank()) return null;
        if (urlOrKey.startsWith("http://") || urlOrKey.startsWith("https://")) return urlOrKey;
        return presignedUrl(urlOrKey);
    }
}
