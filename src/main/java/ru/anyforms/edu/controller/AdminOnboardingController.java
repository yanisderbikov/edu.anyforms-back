package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.SlideRequestDTO;
import ru.anyforms.edu.dto.course.OnboardingResponseDTO;
import ru.anyforms.edu.service.onboarding.OnboardingService;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Редактирование онбординга — только ADMIN (JWT). */
@AllArgsConstructor
@RestController
@RequestMapping("/api/admin/onboarding")
@Tag(name = "AdminOnboarding", description = "Слайды онбординга: тексты, пункты, картинки. Только роль ADMIN")
@SecurityRequirement(name = "Bearer")
public class AdminOnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "Слайды онбординга для админки",
            description = "Как публичный ответ, но с imageKey — сырым значением для редактирования")
    @GetMapping
    public ResponseEntity<OnboardingResponseDTO> getOnboarding() {
        return ResponseEntity.ok(onboardingService.getOnboarding(true));
    }

    @Operation(summary = "Создать слайд",
            description = "kind: TEXT (обычный), SUPPORT (со ссылками), FINAL (последний). "
                    + "В title слово в {фигурных скобках} выделяется акцентом")
    @PostMapping("/slides")
    public ResponseEntity<Map<String, String>> createSlide(@Valid @RequestBody SlideRequestDTO request) {
        UUID id = onboardingService.createSlide(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id.toString()));
    }

    @Operation(summary = "Обновить слайд",
            description = "После сохранения слайды перенумеровываются по порядку: 1, 2, 3…")
    @PutMapping("/slides/{slideId}")
    public ResponseEntity<Void> updateSlide(@PathVariable UUID slideId,
                                            @Valid @RequestBody SlideRequestDTO request) {
        onboardingService.updateSlide(slideId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить слайд")
    @DeleteMapping("/slides/{slideId}")
    public ResponseEntity<Void> deleteSlide(@PathVariable UUID slideId) {
        onboardingService.deleteSlide(slideId);
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
