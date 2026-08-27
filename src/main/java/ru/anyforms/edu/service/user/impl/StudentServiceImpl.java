package ru.anyforms.edu.service.user.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.StudentDTO;
import ru.anyforms.edu.dto.admin.StudentRequestDTO;
import ru.anyforms.edu.model.Role;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.repository.GetterServiceUser;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.repository.SaverServiceUser;
import ru.anyforms.edu.repository.SaverStudent;
import ru.anyforms.edu.service.user.StudentService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
class StudentServiceImpl implements StudentService {

    private final GetterStudent getterStudent;
    private final SaverStudent saverStudent;
    private final GetterServiceUser getterServiceUser;
    private final SaverServiceUser saverServiceUser;

    private static StudentDTO toDTO(Student student, boolean admin) {
        return new StudentDTO(student.getId().toString(), student.getEmail(), student.getActive(),
                student.getPlan(), admin ? Role.ADMIN.name() : Role.STUDENT.name());
    }

    private boolean isActiveAdmin(String email) {
        return getterServiceUser.getByEmail(email)
                .map(u -> Boolean.TRUE.equals(u.getActive()))
                .orElse(false);
    }

    private StudentDTO toDTO(Student student) {
        return toDTO(student, isActiveAdmin(student.getEmail()));
    }

    @Override
    public List<StudentDTO> search(String emailQuery) {
        String query = emailQuery == null ? "" : emailQuery.trim();
        List<Student> students = query.isEmpty()
                ? getterStudent.getAll()
                : getterStudent.searchByEmail(query);
        Set<String> adminEmails = getterServiceUser.getActive().stream()
                .map(ServiceUser::getEmail)
                .collect(Collectors.toSet());
        return students.stream()
                .map(s -> toDTO(s, adminEmails.contains(s.getEmail())))
                .toList();
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
    public StudentDTO setActive(UUID id, boolean active) {
        Student student = getById(id);
        student.setActive(active);
        return toDTO(saverStudent.save(student));
    }

    @Override
    public StudentDTO setRole(UUID id, String role, String actorEmail) {
        Student student = getById(id);
        // Себя не разжаловать и не «переназначить» — иначе можно выпилить себя из админки
        if (student.getEmail().equalsIgnoreCase(ServiceUser.normalizeEmail(actorEmail))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя менять права самому себе");
        }

        ServiceUser serviceUser = getterServiceUser.getByEmail(student.getEmail()).orElse(null);
        if (Role.ADMIN.name().equals(role)) {
            if (serviceUser == null) {
                serviceUser = ServiceUser.builder().email(student.getEmail()).build();
            }
            serviceUser.setActive(Boolean.TRUE);
            saverServiceUser.save(serviceUser);
        } else if (serviceUser != null) {
            // Запись не удаляем, а гасим: история остаётся, вернуть права — одно действие
            serviceUser.setActive(Boolean.FALSE);
            saverServiceUser.save(serviceUser);
        }
        return toDTO(student);
    }

    @Override
    public StudentDTO setPlan(UUID id, String plan) {
        Student student = getById(id);
        student.setPlan(plan);
        return toDTO(saverStudent.save(student));
    }

    @Override
    public void delete(UUID id) {
        saverStudent.delete(getById(id));
    }

    private Student getById(UUID id) {
        return getterStudent.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Клиент не найден: " + id));
    }
}
