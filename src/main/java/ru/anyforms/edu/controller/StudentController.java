package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.service.user.StudentService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Клиенты курса (доступ к платформе) — только ADMIN (JWT). */
@AllArgsConstructor
@RestController
@RequestMapping("/api/admin/students")
@Tag(name = "Students", description = "Email'ы клиентов с доступом к курсу. Только роль ADMIN")
@SecurityRequirement(name = "Bearer")
public class StudentController {

    private final StudentService studentService;

    @Data
    @NoArgsConstructor
    @lombok.AllArgsConstructor
    @Builder
    public static class StudentRequestDTO {
        @NotBlank(message = "Не указан email")
        @Email(message = "Некорректный email")
        private String email;
    }

    public record StudentDTO(String id, String email, Boolean active) {
        static StudentDTO from(Student s) {
            return new StudentDTO(s.getId().toString(), s.getEmail(), s.getActive());
        }
    }

    @Operation(summary = "Все клиенты")
    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAll() {
        return ResponseEntity.ok(studentService.getAll().stream().map(StudentDTO::from).toList());
    }

    @Operation(summary = "Дать клиенту доступ к курсу")
    @PostMapping
    public ResponseEntity<StudentDTO> create(@Valid @RequestBody StudentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StudentDTO.from(studentService.create(request.getEmail())));
    }

    @Operation(summary = "Отозвать доступ")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("message", e.getReason() == null ? "Ошибка запроса" : e.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(Map.of("message", message.isBlank() ? "Некорректный запрос" : message));
    }
}
