package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.user.LessonProgress;

import java.util.List;
import java.util.UUID;

public interface ProgressStore {

    List<UUID> getCompletedLessonIds(UUID studentId);

    /** Идемпотентно: первый запуск видео; повторный вызов ничего не меняет. */
    void markStarted(UUID studentId, UUID lessonId);

    /** Идемпотентно: повторная отметка того же урока — не ошибка. Незачатый урок засчитывается сразу и как начатый. */
    void markCompleted(UUID studentId, UUID lessonId);

    /** Все отметки всех студентов — для аналитики */
    List<LessonProgress> getAll();
}
