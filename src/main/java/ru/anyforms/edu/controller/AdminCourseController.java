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
import ru.anyforms.edu.dto.admin.CourseRequestDTO;
import ru.anyforms.edu.dto.admin.LessonFileRequestDTO;
import ru.anyforms.edu.dto.admin.LessonRequestDTO;
import ru.anyforms.edu.dto.admin.ModuleRequestDTO;
import ru.anyforms.edu.dto.admin.PresignUploadRequestDTO;
import ru.anyforms.edu.dto.course.CourseResponseDTO;
import ru.anyforms.edu.service.admin.AdminCourseService;
import ru.anyforms.edu.service.course.CourseService;
import ru.anyforms.edu.service.s3.S3FileStorage;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Админка курса — только ADMIN (JWT). */
@AllArgsConstructor
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Настройка курса: модули, уроки, файлы. Только роль ADMIN")
@SecurityRequirement(name = "Bearer")
public class AdminCourseController {

    private final CourseService courseService;
    private final AdminCourseService adminCourseService;
    private final S3FileStorage s3FileStorage;

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

    @Operation(summary = "Прикрепить файл к уроку",
            description = "Сам файл уже в S3 (см. presign-upload) — здесь сохраняем ключ, имя и размер. "
                    + "Количество файлов у урока не ограничено")
    @PostMapping("/lessons/{lessonId}/files")
    public ResponseEntity<Map<String, String>> addLessonFile(@PathVariable UUID lessonId,
                                                             @Valid @RequestBody LessonFileRequestDTO request) {
        UUID id = adminCourseService.addLessonFile(lessonId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id.toString()));
    }

    @Operation(summary = "Открепить файл от урока")
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteLessonFile(@PathVariable UUID fileId) {
        adminCourseService.deleteLessonFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Подписанный URL для прямой загрузки в S3",
            description = "Файл уходит из браузера сразу в бакет (PUT по uploadUrl), бэкенд только подписывает. "
                    + "Вернувшийся key сохраняем в модуль/урок. Требует CORS на бакете.")
    @PostMapping("/presign-upload")
    public ResponseEntity<Map<String, String>> presignUpload(@Valid @RequestBody PresignUploadRequestDTO request) {
        String prefix = request.getPrefix() == null || request.getPrefix().isBlank()
                ? "course" : request.getPrefix();
        S3FileStorage.PresignedUpload presigned =
                s3FileStorage.presignUpload(request.getFilename(), request.getContentType(), prefix);
        return ResponseEntity.ok(Map.of("uploadUrl", presigned.uploadUrl(), "key", presigned.key()));
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
