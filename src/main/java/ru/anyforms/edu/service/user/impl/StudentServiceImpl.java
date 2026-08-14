package ru.anyforms.edu.service.user.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.StudentDTO;
import ru.anyforms.edu.dto.admin.StudentRequestDTO;
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

    private static StudentDTO toDTO(Student student) {
        return new StudentDTO(student.getId().toString(), student.getEmail(), student.getActive(),
                student.getPlan());
    }

    @Override
    public List<StudentDTO> getAll() {
        return getterStudent.getAll().stream().map(StudentServiceImpl::toDTO).toList();
    }

    @Override
    public StudentDTO create(StudentRequestDTO request) {
        String email = ServiceUser.normalizeEmail(request.getEmail());
        if (getterStudent.getByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Клиент уже существует: " + email);
        }
        return toDTO(saverStudent.save(Student.builder().email(email).build()));
    }

    @Override
    public void delete(UUID id) {
        Student student = getterStudent.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Клиент не найден: " + id));
        saverStudent.delete(student);
    }
}
