package ru.anyforms.edu.service.s3.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.config.s3.S3Static;
import ru.anyforms.edu.service.s3.S3FileStorage;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Slf4j
class S3Service implements S3FileStorage {

    /** Ссылка живёт час — под длительность одного занятия */
    private static final Duration PRESIGN_TTL = Duration.ofHours(1);
    /** Окно на прямую загрузку файла из браузера в S3 */
    private static final Duration UPLOAD_TTL = Duration.ofMinutes(30);
    private static final Pattern EXTENSION = Pattern.compile("\\.[a-z0-9]{1,10}$");

    // ObjectProvider: бины S3 существуют только при заданных ключах (см. S3Config),
    // без них приложение работает, но upload/presign вернут понятную ошибку
    private final ObjectProvider<S3Client> s3ClientProvider;
    private final ObjectProvider<S3Presigner> s3PresignerProvider;
    private final S3Static s3Static;

    private S3Client requireClient() {
        S3Client client = s3ClientProvider.getIfAvailable();
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "S3 не настроен: заполните S3_ACCESS_KEY_ID и S3_SECRET_ACCESS_KEY в .env");
        }
        return client;
    }

    @Override
    public PresignedUpload presignUpload(String filename, String contentType, String keyPrefix) {
        S3Presigner presigner = s3PresignerProvider.getIfAvailable();
        if (presigner == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "S3 не настроен: заполните S3_ACCESS_KEY_ID и S3_SECRET_ACCESS_KEY в .env");
        }
        String original = filename == null ? "" : filename.toLowerCase();
        var matcher = EXTENSION.matcher(original);
        String ext = matcher.find() ? matcher.group() : "";
        String key = keyPrefix + "/" + UUID.randomUUID() + ext;

        PutObjectRequest.Builder put = PutObjectRequest.builder()
                .bucket(s3Static.getBucketName())
                .key(key);
        // Content-Type входит в подпись: браузер должен прислать тот же заголовок
        if (contentType != null && !contentType.isBlank()) {
            put.contentType(contentType);
        }
        String uploadUrl = presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(UPLOAD_TTL)
                        .putObjectRequest(put.build())
                        .build())
                .url().toString();
        return new PresignedUpload(uploadUrl, key);
    }

    @Override
    public String upload(MultipartFile file, String keyPrefix) {
        S3Client client = requireClient();
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        var matcher = EXTENSION.matcher(original);
        String ext = matcher.find() ? matcher.group() : "";
        String key = keyPrefix + "/" + UUID.randomUUID() + ext;

        // Загрузка через временный файл: SDK сам считает длину и SHA-256,
        // иначе Yandex отдаёт SignatureDoesNotMatch на chunked-подписи
        File tmp = null;
        try {
            tmp = File.createTempFile("edu-upload-", ext.isEmpty() ? ".bin" : ext);
            file.transferTo(tmp);
            client.putObject(PutObjectRequest.builder()
                            .bucket(s3Static.getBucketName())
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    tmp.toPath());
            return key;
        } catch (IOException e) {
            log.error("upload to S3 failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось загрузить файл");
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp.toPath());
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void delete(String key) {
        requireClient().deleteObject(DeleteObjectRequest.builder()
                .bucket(s3Static.getBucketName())
                .key(key)
                .build());
    }

    @Override
    public String presignedUrl(String key) {
        S3Presigner presigner = s3PresignerProvider.getIfAvailable();
        if (presigner == null) {
            // S3 не настроен: ключи в БД не резолвим, но и не падаем
            log.warn("S3 не настроен, не могу подписать ключ: {}", key);
            return null;
        }
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Static.getBucketName())
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_TTL)
                .getObjectRequest(getObjectRequest)
                .build();
        return presigner.presignGetObject(presignRequest).url().toString();
    }
}
