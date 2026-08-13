package ru.anyforms.edu.service.user;

import ru.anyforms.edu.model.user.Student;

import java.util.List;
import java.util.UUID;

public interface StudentService {

    List<Student> getAll();

    Student create(String email);

    void delete(UUID id);
}
