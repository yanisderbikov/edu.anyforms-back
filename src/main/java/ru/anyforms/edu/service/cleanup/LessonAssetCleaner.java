package ru.anyforms.edu.service.cleanup;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.anyforms.edu.model.course.Lesson;
import ru.anyforms.edu.model.course.LessonFile;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.service.kinescope.KinescopeService;
import ru.anyforms.edu.service.s3.S3FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Подчищает файлы удалённого урока во внешних хранилищах: видео — в Kinescope,
 * обложку и материалы — в S3.
 * <p>
 * Внешние удаления необратимы и не умеют откатываться вместе с транзакцией,
 * поэтому уборка запускается строго после её успешного коммита: сначала база
 * говорит «урок удалён», и только потом пропадают файлы. Обратный порядок дал бы
 * худший исход — файлов нет, а урок на месте.
 */
@Component
@AllArgsConstructor
@Slf4j
public class LessonAssetCleaner {

    private static final int MAX_PARALLEL = 6;

    /** Демоны: незавершённая уборка не должна держать остановку приложения */
    private static final java.util.concurrent.ThreadFactory THREADS = runnable -> {
        Thread thread = new Thread(runnable, "lesson-cleanup");
        thread.setDaemon(true);
        return thread;
    };

    private final S3FileStorage s3FileStorage;
    private final KinescopeService kinescopeService;
    private final GetterCourse getterCourse;

    /** Что у урока лежит во внешних хранилищах. Собирается внутри транзакции, пока жива сессия. */
    public record Assets(String videoUrl, String coverUrl, List<String> fileUrls) {

        public static Assets of(Lesson lesson) {
            return new Assets(
                    lesson.getVideoUrl(),
                    lesson.getCoverUrl(),
                    lesson.getFiles().stream()
                            .map(LessonFile::getFileUrl)
                            .filter(Objects::nonNull)
                            .toList());
        }

        /** Один открепляемый файл-материал. */
        public static Assets ofFile(String fileUrl) {
            return new Assets(null, null, fileUrl == null ? List.of() : List.of(fileUrl));
        }

        /** Видео, вытесненное из урока новым: удаляем так же, как при удалении урока. */
        public static Assets ofVideo(String videoUrl) {
            return new Assets(videoUrl, null, List.of());
        }

        /** Вытесненная обложка. */
        public static Assets ofCover(String coverUrl) {
            return new Assets(null, coverUrl, List.of());
        }
    }

    /**
     * Ставит файлы в очередь на удаление: они уйдут из Kinescope и S3 сразу после того,
     * как транзакция успешно закоммитится. Без активной транзакции удаляет сразу.
     */
    public void deleteAfterCommit(Assets assets) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteNow(assets);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteNow(assets);
            }
        });
    }

    /** Удаляет всё разом: задачи идут параллельно, упавшая не мешает остальным. */
    void deleteNow(Assets assets) {
        List<Task> tasks = new ArrayList<>();

        String videoId = KinescopeService.extractVideoId(assets.videoUrl());
        if (videoId != null) {
            if (stillInUse(assets.videoUrl())) {
                log.info("Видео {} осталось в Kinescope: на него ссылается другой урок", videoId);
            } else {
                tasks.add(new Task("видео " + videoId + " в Kinescope",
                        () -> kinescopeService.deleteVideo(videoId)));
            }
        } else {
            addS3Task(tasks, assets.videoUrl(), "видео");
        }
        addS3Task(tasks, assets.coverUrl(), "обложку");
        assets.fileUrls().forEach(url -> addS3Task(tasks, url, "материал"));

        if (tasks.isEmpty()) {
            return;
        }
        // Ждём все задачи: таймауты — на стороне клиентов S3 и Kinescope,
        // а упасть allOf не может, каждая задача гасит свои ошибки сама
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(tasks.size(), MAX_PARALLEL), THREADS);
        try {
            CompletableFuture<?>[] running = tasks.stream()
                    .map(task -> CompletableFuture.runAsync(task::runQuietly, pool))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(running).join();
        } finally {
            pool.shutdown();
        }
    }

    /** http-ссылки пропускаем: это чужой файл, не наш объект в бакете. */
    private void addS3Task(List<Task> tasks, String urlOrKey, String what) {
        if (urlOrKey == null || urlOrKey.isBlank()
                || urlOrKey.startsWith("http://") || urlOrKey.startsWith("https://")) {
            return;
        }
        if (stillInUse(urlOrKey)) {
            log.info("Файл {} остался в бакете: на него ссылается другой урок", urlOrKey);
            return;
        }
        tasks.add(new Task(what + " " + urlOrKey + " в S3", () -> s3FileStorage.delete(urlOrKey)));
    }

    /**
     * Один и тот же файл можно вставить в два урока — тогда удаление одного
     * не должно ломать второй. Удалённый урок в подсчёт уже не попадает.
     */
    private boolean stillInUse(String urlOrKey) {
        try {
            return getterCourse.isAssetInUse(urlOrKey);
        } catch (Exception e) {
            // Не смогли проверить — безопаснее оставить файл, чем удалить нужный
            log.error("Не удалось проверить, используется ли {} — файл оставлен", urlOrKey, e);
            return true;
        }
    }

    private record Task(String what, Runnable action) {

        void runQuietly() {
            try {
                action.run();
            } catch (Exception e) {
                // Файл останется сиротой — данные при этом целы, чинится удалением вручную
                log.error("Не удалось удалить {} — файл остался в хранилище", what, e);
            }
        }
    }
}
