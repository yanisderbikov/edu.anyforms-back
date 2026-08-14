package ru.anyforms.edu.repository;

import java.util.List;
import java.util.UUID;

public interface ProgressStore {

    List<UUID> getCompletedLessonIds(UUID studentId);

    /** Идемпотентно: повторная отметка того же урока — не ошибка. */
    void markCompleted(UUID studentId, UUID lessonId);
}
