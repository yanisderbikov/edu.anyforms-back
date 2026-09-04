package ru.anyforms.edu.service.activity.impl;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.anyforms.edu.repository.ActivityStore;
import ru.anyforms.edu.service.activity.ActivityTracker;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Отметки уходят в БД из одного фонового потока: страница модуля отдаётся
 * в read-only транзакции, и писать туда нельзя, да и незачем — студенту
 * ответ важнее статистики. Очередь ограничена: если БД встала, отметки
 * теряются, а не копятся в памяти до бесконечности.
 */
@Service
@Slf4j
class ActivityTrackerImpl implements ActivityTracker {

    private static final int QUEUE_CAPACITY = 1000;

    private final ActivityStore activityStore;
    private final ThreadPoolExecutor executor;

    ActivityTrackerImpl(ActivityStore activityStore) {
        this.activityStore = activityStore;
        this.executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                runnable -> {
                    Thread thread = new Thread(runnable, "activity-tracker");
                    thread.setDaemon(true);
                    return thread;
                },
                (runnable, pool) -> log.warn("Очередь отметок активности переполнена — отметка пропущена"));
    }

    @Override
    public void moduleVisited(UUID studentId, UUID moduleId) {
        executor.execute(() -> {
            try {
                activityStore.recordModuleVisit(studentId, moduleId);
            } catch (Exception e) {
                // Аналитика — не повод шуметь исключениями: логируем и живём дальше
                log.warn("Не удалось записать заход студента {} в модуль {}", studentId, moduleId, e);
            }
        });
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}
