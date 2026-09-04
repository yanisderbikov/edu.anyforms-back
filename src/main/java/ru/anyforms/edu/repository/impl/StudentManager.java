package ru.anyforms.edu.repository.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.repository.SaverStudent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
class StudentManager implements GetterStudent, SaverStudent {

    /** Свежее этого last_seen_at не обновляем: точность в минуты для аналитики достаточна */
    private static final Duration SEEN_THROTTLE = Duration.ofMinutes(5);

    private final StudentRepo studentRepo;

    @Override
    public List<Student> getAll() {
        try {
            return studentRepo.findAllByOrderByCreatedAtDesc();
        } catch (Exception e) {
            log.error("getAll failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public List<Student> searchByEmail(String emailPart) {
        try {
            return studentRepo.findByEmailContainingIgnoreCaseOrderByCreatedAtDesc(emailPart);
        } catch (Exception e) {
            log.error("searchByEmail failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public Optional<Student> getByEmail(String email) {
        try {
            return studentRepo.findByEmail(ServiceUser.normalizeEmail(email));
        } catch (Exception e) {
            log.error("getByEmail failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public Optional<Student> getById(UUID id) {
        try {
            return studentRepo.findById(id);
        } catch (Exception e) {
            log.error("getById failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public Student save(Student student) {
        try {
            return studentRepo.save(student);
        } catch (Exception e) {
            log.error("save failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public void delete(Student student) {
        try {
            studentRepo.delete(student);
        } catch (Exception e) {
            log.error("delete failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    @Transactional
    public void touchSeen(Student student) {
        Instant now = Instant.now();
        Instant threshold = now.minus(SEEN_THROTTLE);
        Instant last = student.getLastSeenAt();
        if (last != null && !last.isBefore(threshold)) {
            return; // отметка свежая — в БД не ходим
        }
        try {
            studentRepo.touchSeen(student.getId(), now, threshold);
        } catch (Exception e) {
            log.error("touchSeen failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
