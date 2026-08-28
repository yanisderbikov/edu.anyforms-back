package ru.anyforms.edu.service.s3;

import org.springframework.web.multipart.MultipartFile;

public interface S3FileStorage {

    /** Подписанный URL для прямой загрузки из браузера в S3 (PUT), минуя бэкенд. */
    record PresignedUpload(String uploadUrl, String key) {
    }

    PresignedUpload presignUpload(String filename, String contentType, String keyPrefix);

    /** Загружает файл в бакет, возвращает ключ объекта. */
    String upload(MultipartFile file, String keyPrefix);

    void delete(String key);

    /** Временная подписанная ссылка на объект. */
    String presignedUrl(String key);

    /**
     * Временная подписанная ссылка на скачивание: браузер сохранит файл
     * под переданным именем, а не под ключом-UUID из бакета.
     */
    String presignedDownloadUrl(String key, String downloadName);

    /**
     * imageUrl/videoUrl в БД может быть и полным URL, и ключом S3:
     * http(s)-ссылки отдаём как есть, ключи превращаем в presigned URL.
     */
    default String resolveUrl(String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank()) return null;
        if (urlOrKey.startsWith("http://") || urlOrKey.startsWith("https://")) return urlOrKey;
        return presignedUrl(urlOrKey);
    }

    /** То же для скачиваемых файлов: у ключей S3 задаём имя сохранения. */
    default String resolveDownloadUrl(String urlOrKey, String downloadName) {
        if (urlOrKey == null || urlOrKey.isBlank()) return null;
        if (urlOrKey.startsWith("http://") || urlOrKey.startsWith("https://")) return urlOrKey;
        return presignedDownloadUrl(urlOrKey, downloadName);
    }
}
