package ru.anyforms.edu.service.kinescope;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Интеграция с Kinescope — видеохостингом уроков.
 * Видео хранятся и защищаются у них (домены, DRM, вотермарка),
 * наш бэкенд решает две задачи: выдать ссылку загрузки админке
 * и подтверждать Kinescope, что зритель — наш живой студент.
 */
public interface KinescopeService {

    /** Ссылка урока → id видео: https://kinescope.io/embed/{id} и её варианты */
    Pattern VIDEO_ID = Pattern.compile("kinescope\\.io/(?:embed/)?([A-Za-z0-9_-]{8,})");

    /** id видео из ссылки; null — ссылка не на Kinescope (старый ключ S3 или чужой URL) */
    static String extractVideoId(String urlOrKey) {
        if (urlOrKey == null) {
            return null;
        }
        Matcher matcher = VIDEO_ID.matcher(urlOrKey);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** Одноразовая ссылка прямой загрузки: файл идёт из браузера сразу в Kinescope. */
    record UploadLink(String videoId, String endpoint, String embedUrl) {
    }

    /** Короткоживущий токен воспроизведения — плеер передаёт его как drmauthtoken. */
    record VideoToken(String token, long expiresInSeconds) {
    }

    UploadLink createUploadLink(String filename, long filesize, String title);

    VideoToken createVideoToken(String email);

    /**
     * Колбэк «пускать ли зрителя»: Kinescope присылает наш же токен воспроизведения,
     * прежде чем выдать плееру ключи. true — играть, false — доступ запрещён.
     */
    boolean authorizePlayback(String token);

    /** Удаляет видео из кабинета Kinescope. Необратимо; отсутствующее видео — не ошибка. */
    void deleteVideo(String videoId);
}
