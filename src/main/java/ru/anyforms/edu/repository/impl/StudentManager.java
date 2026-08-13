package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.repository.SaverStudent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
class StudentManager implements GetterStudent, SaverStudent {

    private final StudentRepo studentRepo;

    @Override
    public List<Student> getAll() {
        try {
            return studentRepo.findAll();
        } catch (Exception e) {
            log.error("getAll failed", e);
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
}
