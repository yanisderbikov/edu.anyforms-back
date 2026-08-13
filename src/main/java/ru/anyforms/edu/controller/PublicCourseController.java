package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.anyforms.edu.dto.course.CourseResponseDTO;
import ru.anyforms.edu.service.course.CourseService;

/** Контент курса — только для залогиненных (ADMIN или STUDENT). */
@AllArgsConstructor
@RestController
@RequestMapping("/api/course")
@Tag(name = "Course", description = "Данные курса для платформы (требуется JWT)")
public class PublicCourseController {

    private final CourseService courseService;

    @Operation(summary = "Курс целиком",
            description = "Курс, модули с уроками (уроки только у открытых модулей), ссылки поддержки",
            security = @SecurityRequirement(name = "Bearer"))
    @GetMapping
    public ResponseEntity<CourseResponseDTO> getCourse() {
        return ResponseEntity.ok(courseService.getPublicCourse());
    }
}
