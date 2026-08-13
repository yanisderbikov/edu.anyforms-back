package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.ServiceUserRequestDTO;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.service.user.ServiceUserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Сервисные пользователи (админы). Вход по email появится позже. */
@AllArgsConstructor
@RestController
@RequestMapping("/api/admin/service-users")
@Tag(name = "ServiceUsers", description = "Email'ы сервисных пользователей")
public class ServiceUserController {

    private final ServiceUserService serviceUserService;

    public record ServiceUserDTO(String id, String email, String role, Boolean active) {
        static ServiceUserDTO from(ServiceUser u) {
            return new ServiceUserDTO(u.getId().toString(), u.getEmail(), u.getRole(), u.getActive());
        }
    }

    @Operation(summary = "Все сервисные пользователи")
    @GetMapping
    public ResponseEntity<List<ServiceUserDTO>> getAll() {
        return ResponseEntity.ok(serviceUserService.getAll().stream().map(ServiceUserDTO::from).toList());
    }

    @Operation(summary = "Добавить сервисного пользователя")
    @PostMapping
    public ResponseEntity<ServiceUserDTO> create(@Valid @RequestBody ServiceUserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ServiceUserDTO.from(serviceUserService.create(request)));
    }

    @Operation(summary = "Удалить сервисного пользователя")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceUserService.delete(id);
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
