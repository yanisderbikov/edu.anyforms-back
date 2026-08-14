package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.user.LessonProgress;
import ru.anyforms.edu.repository.ProgressStore;

import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
class ProgressManager implements ProgressStore {

    private final LessonProgressRepo lessonProgressRepo;

    @Override
    public List<UUID> getCompletedLessonIds(UUID studentId) {
        try {
            return lessonProgressRepo.findByStudentId(studentId).stream()
                    .map(LessonProgress::getLessonId)
                    .toList();
        } catch (Exception e) {
            log.error("getCompletedLessonIds failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public void markCompleted(UUID studentId, UUID lessonId) {
        try {
            if (lessonProgressRepo.existsByStudentIdAndLessonId(studentId, lessonId)) {
                return;
            }
            lessonProgressRepo.save(LessonProgress.builder()
                    .studentId(studentId)
                    .lessonId(lessonId)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // Гонка двух запросов: уникальный индекс уже отметил урок — это не ошибка
            log.debug("Урок {} уже отмечен у {}", lessonId, studentId);
        } catch (Exception e) {
            log.error("markCompleted failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
