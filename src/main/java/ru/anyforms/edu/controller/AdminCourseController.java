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
import ru.anyforms.edu.dto.admin.CourseRequestDTO;
import ru.anyforms.edu.dto.admin.LessonRequestDTO;
import ru.anyforms.edu.dto.admin.ModuleRequestDTO;
import ru.anyforms.edu.dto.course.CourseResponseDTO;
import ru.anyforms.edu.service.admin.AdminCourseService;
import ru.anyforms.edu.service.course.CourseService;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Админка курса. TODO: закрыть ролью ADMIN после появления email-логина. */
@AllArgsConstructor
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Настройка курса: модули, уроки, файлы")
public class AdminCourseController {

    private final CourseService courseService;
    private final AdminCourseService adminCourseService;

    @Operation(summary = "Курс целиком для админки", description = "Все модули и уроки, включая закрытые")
    @GetMapping("/course")
    public ResponseEntity<CourseResponseDTO> getCourse() {
        return ResponseEntity.ok(courseService.getAdminCourse());
    }

    @Operation(summary = "Обновить шапку курса")
    @PutMapping("/course")
    public ResponseEntity<Void> updateCourse(@Valid @RequestBody CourseRequestDTO request) {
        adminCourseService.updateCourse(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Создать модуль")
    @PostMapping("/modules")
    public ResponseEntity<Map<String, String>> createModule(@Valid @RequestBody ModuleRequestDTO request) {
        UUID id = adminCourseService.createModule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id.toString()));
    }

    @Operation(summary = "Обновить модуль")
    @PutMapping("/modules/{moduleId}")
    public ResponseEntity<Void> updateModule(@PathVariable UUID moduleId,
                                             @Valid @RequestBody ModuleRequestDTO request) {
        adminCourseService.updateModule(moduleId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить модуль (вместе с уроками)")
    @DeleteMapping("/modules/{moduleId}")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID moduleId) {
        adminCourseService.deleteModule(moduleId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Создать урок в модуле")
    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<Map<String, String>> createLesson(@PathVariable UUID moduleId,
                                                            @Valid @RequestBody LessonRequestDTO request) {
        UUID id = adminCourseService.createLesson(moduleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id.toString()));
    }

    @Operation(summary = "Обновить урок")
    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> updateLesson(@PathVariable UUID lessonId,
                                             @Valid @RequestBody LessonRequestDTO request) {
        adminCourseService.updateLesson(lessonId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить урок")
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID lessonId) {
        adminCourseService.deleteLesson(lessonId);
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
