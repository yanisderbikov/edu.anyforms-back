package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            return lessonProgressRepo.findByStudentIdAndCompletedAtIsNotNull(studentId).stream()
                    .map(LessonProgress::getLessonId)
                    .toList();
        } catch (Exception e) {
            log.error("getCompletedLessonIds failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public void markStarted(UUID studentId, UUID lessonId) {
        try {
            lessonProgressRepo.insertStarted(studentId, lessonId);
        } catch (Exception e) {
            log.error("markStarted failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public void markCompleted(UUID studentId, UUID lessonId) {
        try {
            lessonProgressRepo.upsertCompleted(studentId, lessonId);
        } catch (Exception e) {
            log.error("markCompleted failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public List<LessonProgress> getAll() {
        try {
            return lessonProgressRepo.findAll();
        } catch (Exception e) {
            log.error("getAll failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
