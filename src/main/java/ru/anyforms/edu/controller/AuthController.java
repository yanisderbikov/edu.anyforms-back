package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.auth.AuthResponseDTO;
import ru.anyforms.edu.dto.auth.RequestCodeDTO;
import ru.anyforms.edu.dto.auth.VerifyCodeDTO;
import ru.anyforms.edu.service.auth.AuthService;

import java.util.Map;
import java.util.stream.Collectors;

/** Вход по коду с почты: request-code → письмо → verify → JWT. */
@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Вход по коду с почты. JWT живёт месяц; у клиентов новый вход гасит старые токены")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Отправить код входа на почту",
            description = "Доступ клиента проверяется в anyforms-5 (оплаченная покупка курса), "
                    + "админы берутся из service_user")
    @PostMapping("/request-code")
    public ResponseEntity<Map<String, String>> requestCode(@Valid @RequestBody RequestCodeDTO request) {
        authService.requestCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Код отправлен на почту"));
    }

    @Operation(summary = "Обменять код на JWT",
            description = "Ответ: token (Bearer для Authorize в Swagger), role (ADMIN | STUDENT), email")
    @PostMapping("/verify")
    public ResponseEntity<AuthResponseDTO> verify(@Valid @RequestBody VerifyCodeDTO request) {
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
