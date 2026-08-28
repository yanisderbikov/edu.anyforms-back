package ru.anyforms.edu.dto.me;

import java.util.List;

/** Прогресс текущего пользователя: онбординг и просмотренные уроки. */
public record ProgressDTO(boolean onboardingDone, List<String> completedLessonIds) {
}
