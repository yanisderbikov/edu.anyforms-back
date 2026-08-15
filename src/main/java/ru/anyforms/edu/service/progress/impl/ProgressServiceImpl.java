package ru.anyforms.edu.service.progress.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.me.ProgressDTO;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.repository.ProgressStore;
import ru.anyforms.edu.repository.SaverStudent;
import ru.anyforms.edu.service.progress.ProgressService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
class ProgressServiceImpl implements ProgressService {

    private final GetterStudent getterStudent;
    private final SaverStudent saverStudent;
    private final GetterCourse getterCourse;
    private final ProgressStore progressStore;

    private Student requireStudent(String email) {
        return getterStudent.getByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Клиент не найден: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressDTO getProgress(String email, boolean admin) {
        // Админ — не студент: онбординг ему не показываем, прогресс не ведём
        if (admin) {
            return new ProgressDTO(true, List.of());
        }
        return toDTO(requireStudent(email));
    }

    @Override
    @Transactional
    public ProgressDTO finishOnboarding(String email, boolean admin) {
        if (admin) {
            return getProgress(email, true);
        }
        Student student = requireStudent(email);
        if (student.getOnboardingDoneAt() == null) {
            student.setOnboardingDoneAt(Instant.now());
            saverStudent.save(student);
        }
        return toDTO(student);
    }

    @Override
    @Transactional
    public ProgressDTO completeLesson(String email, boolean admin, UUID lessonId) {
        if (admin) {
            return getProgress(email, true);
        }
        Student student = requireStudent(email);
        getterCourse.getLessonById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Урок не найден: " + lessonId));
        progressStore.markCompleted(student.getId(), lessonId);
        return toDTO(student);
    }

    private ProgressDTO toDTO(Student student) {
        List<String> completed = progressStore.getCompletedLessonIds(student.getId()).stream()
                .map(UUID::toString)
                .toList();
        return new ProgressDTO(student.getOnboardingDoneAt() != null, completed);
    }
}
