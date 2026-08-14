package ru.anyforms.edu.service.user;

import ru.anyforms.edu.dto.admin.StudentDTO;
import ru.anyforms.edu.dto.admin.StudentRequestDTO;

import java.util.List;
import java.util.UUID;

public interface StudentService {

    List<StudentDTO> getAll();

    StudentDTO create(StudentRequestDTO request);

    void delete(UUID id);
}
