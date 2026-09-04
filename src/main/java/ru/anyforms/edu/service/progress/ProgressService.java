package ru.anyforms.edu.service.progress;

import ru.anyforms.edu.dto.me.ProgressDTO;

import java.util.UUID;

public interface ProgressService {

    ProgressDTO getProgress(String email, boolean admin);

    /** Возвращает актуальный прогресс, чтобы клиенту не нужен был повторный GET */
    ProgressDTO finishOnboarding(String email, boolean admin);

    /** Первый запуск видео урока. Идемпотентно: повторный вызов ничего не меняет */
    ProgressDTO startLesson(String email, boolean admin, UUID lessonId);

    ProgressDTO completeLesson(String email, boolean admin, UUID lessonId);
}
