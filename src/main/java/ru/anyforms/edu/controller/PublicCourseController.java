package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.course.CourseResponseDTO;
import ru.anyforms.edu.service.course.CourseService;

import java.util.Map;
import java.util.UUID;

/** Контент курса — только для залогиненных (ADMIN или STUDENT). */
@AllArgsConstructor
@RestController
@RequestMapping("/api/course")
@Tag(name = "Course", description = "Данные курса для платформы (требуется JWT)")
public class PublicCourseController {

    private final CourseService courseService;

    private static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @Operation(summary = "Курс и превью модулей",
            description = "Шапка курса, ссылки поддержки и модули без уроков — только счётчики "
                    + "lessonsCount/lessonsDone для карточек. Уроки берутся по одному модулю",
            security = @SecurityRequirement(name = "Bearer"))
    @GetMapping
    public ResponseEntity<CourseResponseDTO> getCourse(Authentication authentication) {
        return ResponseEntity.ok(courseService.getPublicCourse(authentication.getName()));
    }

    @Operation(summary = "Один модуль с уроками",
            description = "Студенту уроки закрытого модуля не отдаются: приходит статус locked и пустой "
                    + "список. Админу закрытый модуль приходит как открытый — проверить страницу до даты открытия",
            security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<CourseResponseDTO.ModuleDTO> getModule(Authentication authentication,
                                                                 @PathVariable UUID moduleId) {
        return ResponseEntity.ok(courseService.getPublicModule(
                authentication.getName(), isAdmin(authentication), moduleId));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("message", e.getReason() == null ? "Ошибка запроса" : e.getReason()));
    }
}
