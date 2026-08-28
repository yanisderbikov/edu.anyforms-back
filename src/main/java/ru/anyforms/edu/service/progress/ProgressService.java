package ru.anyforms.edu.service.progress;

import ru.anyforms.edu.dto.me.ProgressDTO;

import java.util.UUID;

public interface ProgressService {

    ProgressDTO getProgress(String email, boolean admin);

    /** Возвращает актуальный прогресс, чтобы клиенту не нужен был повторный GET */
    ProgressDTO finishOnboarding(String email, boolean admin);

    ProgressDTO completeLesson(String email, boolean admin, UUID lessonId);
}
