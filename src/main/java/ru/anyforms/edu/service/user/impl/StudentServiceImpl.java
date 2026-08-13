package ru.anyforms.edu.service.user.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.repository.SaverStudent;
import ru.anyforms.edu.service.user.StudentService;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
class StudentServiceImpl implements StudentService {

    private final GetterStudent getterStudent;
    private final SaverStudent saverStudent;

    @Override
    public List<Student> getAll() {
        return getterStudent.getAll();
    }

    @Override
    public Student create(String rawEmail) {
        String email = ServiceUser.normalizeEmail(rawEmail);
        if (getterStudent.getByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Клиент уже существует: " + email);
        }
        return saverStudent.save(Student.builder().email(email).build());
    }

    @Override
    public void delete(UUID id) {
        Student student = getterStudent.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Клиент не найден: " + id));
        saverStudent.delete(student);
    }
}
