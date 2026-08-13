package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.user.Student;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetterStudent {

    List<Student> getAll();

    Optional<Student> getByEmail(String email);

    Optional<Student> getById(UUID id);
}
