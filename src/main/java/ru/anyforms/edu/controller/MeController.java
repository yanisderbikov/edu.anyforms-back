package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.me.ProgressDTO;
import ru.anyforms.edu.service.progress.ProgressService;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Прогресс текущего пользователя (по JWT): онбординг и просмотренные уроки. */
@AllArgsConstructor
@RestController
@RequestMapping("/api/me")
@Tag(name = "Me", description = "Прогресс текущего пользователя (требуется JWT)")
@SecurityRequirement(name = "Bearer")
public class MeController {

    private final ProgressService progressService;

    private static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @Operation(summary = "Мой прогресс",
            description = "Пройден ли онбординг и какие уроки просмотрены полностью. У админов онбординг всегда пройден")
    @GetMapping("/progress")
    public ResponseEntity<ProgressDTO> getProgress(Authentication auth) {
        return ResponseEntity.ok(progressService.getProgress(auth.getName(), isAdmin(auth)));
    }

    @Operation(summary = "Онбординг пройден")
    @PostMapping("/onboarding-done")
    public ResponseEntity<Void> finishOnboarding(Authentication auth) {
        progressService.finishOnboarding(auth.getName(), isAdmin(auth));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Урок просмотрен полностью", description = "Идемпотентно: повторный вызов — не ошибка")
    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<Void> completeLesson(Authentication auth, @PathVariable UUID lessonId) {
        progressService.completeLesson(auth.getName(), isAdmin(auth), lessonId);
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
