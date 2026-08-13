package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.service.auth.AuthService;

import java.util.Map;
import java.util.stream.Collectors;

/** Вход по коду с почты: request-code → письмо → verify → JWT. */
@AllArgsConstructor
@RestController
@RequestMapping("/api/public/auth")
@Tag(name = "Auth", description = "Вход по коду с почты. JWT живёт месяц; у клиентов новый вход гасит старые токены")
public class AuthController {

    private final AuthService authService;

    @Data
    @NoArgsConstructor
    @lombok.AllArgsConstructor
    @Builder
    public static class RequestCodeDTO {
        @NotBlank(message = "Не указан e-mail")
        @Email(message = "Некорректный e-mail")
        private String email;
    }

    @Data
    @NoArgsConstructor
    @lombok.AllArgsConstructor
    @Builder
    public static class VerifyDTO {
        @NotBlank(message = "Не указан e-mail")
        @Email(message = "Некорректный e-mail")
        private String email;

        @NotBlank(message = "Не указан код")
        private String code;
    }

    @Operation(summary = "Отправить код входа на почту",
            description = "E-mail должен иметь доступ: админ (service_user) или клиент (student)")
    @PostMapping("/request-code")
    public ResponseEntity<Map<String, String>> requestCode(@Valid @RequestBody RequestCodeDTO request) {
        authService.requestCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Код отправлен на почту"));
    }

    @Operation(summary = "Обменять код на JWT",
            description = "Ответ: token (Bearer для Authorize в Swagger), role (ADMIN | STUDENT), email")
    @PostMapping("/verify")
    public ResponseEntity<AuthService.AuthResult> verify(@Valid @RequestBody VerifyDTO request) {
        return ResponseEntity.ok(authService.verify(request.getEmail(), request.getCode()));
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
