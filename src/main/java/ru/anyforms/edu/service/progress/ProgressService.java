package ru.anyforms.edu.service.progress;

import ru.anyforms.edu.dto.me.ProgressDTO;

import java.util.UUID;

public interface ProgressService {

    ProgressDTO getProgress(String email, boolean admin);

    void finishOnboarding(String email, boolean admin);

    void completeLesson(String email, boolean admin, UUID lessonId);
}
