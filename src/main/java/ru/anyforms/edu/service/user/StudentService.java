package ru.anyforms.edu.service.user;

import ru.anyforms.edu.dto.admin.StudentDTO;
import ru.anyforms.edu.dto.admin.StudentRequestDTO;

import java.util.List;
import java.util.UUID;

public interface StudentService {

    List<StudentDTO> search(String emailQuery);

    StudentDTO create(StudentRequestDTO request);

    StudentDTO setActive(UUID id, boolean active);

    StudentDTO setRole(UUID id, String role, String actorEmail);

    StudentDTO setPlan(UUID id, String plan);

    void delete(UUID id);
}
