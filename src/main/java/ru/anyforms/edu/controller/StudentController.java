package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.StudentActiveRequestDTO;
import ru.anyforms.edu.dto.admin.StudentDTO;
import ru.anyforms.edu.dto.admin.StudentPlanRequestDTO;
import ru.anyforms.edu.dto.admin.StudentRequestDTO;
import ru.anyforms.edu.dto.admin.StudentRoleRequestDTO;
import ru.anyforms.edu.dto.admin.StudentsBulkRequestDTO;
import ru.anyforms.edu.dto.admin.StudentsBulkResultDTO;
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

    @Operation(summary = "Клиенты: весь список или поиск по части email")
    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAll(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(studentService.search(search));
    }

    @Operation(summary = "Дать клиенту доступ к курсу")
    @PostMapping
    public ResponseEntity<StudentDTO> create(@Valid @RequestBody StudentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @Operation(summary = "Дать доступ списком email — уже заведённых не меняет")
    @PostMapping("/bulk")
    public ResponseEntity<StudentsBulkResultDTO> createBulk(@Valid @RequestBody StudentsBulkRequestDTO request) {
        return ResponseEntity.ok(studentService.createBulk(request));
    }

    @Operation(summary = "Включить/отключить доступ (отключённого не вернёт даже покупка)")
    @PatchMapping("/{id}/active")
    public ResponseEntity<StudentDTO> setActive(@PathVariable UUID id,
                                                @Valid @RequestBody StudentActiveRequestDTO request) {
        return ResponseEntity.ok(studentService.setActive(id, request.getActive()));
    }

    @Operation(summary = "Назначить права: ADMIN — доступ в админку, STUDENT — забрать его")
    @PatchMapping("/{id}/role")
    public ResponseEntity<StudentDTO> setRole(@PathVariable UUID id,
                                              @Valid @RequestBody StudentRoleRequestDTO request,
                                              Authentication authentication) {
        return ResponseEntity.ok(studentService.setRole(id, request.getRole(), authentication.getName()));
    }

    @Operation(summary = "Формат обучения: SELF — общий, PERSONAL — персональный")
    @PatchMapping("/{id}/plan")
    public ResponseEntity<StudentDTO> setPlan(@PathVariable UUID id,
                                              @Valid @RequestBody StudentPlanRequestDTO request) {
        return ResponseEntity.ok(studentService.setPlan(id, request.getPlan()));
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
