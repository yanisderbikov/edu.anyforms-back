package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.AnalyticsDTO;
import ru.anyforms.edu.service.analytics.AnalyticsService;

import java.util.Map;

/** Аналитика прогресса клиентов — только ADMIN (JWT). */
@AllArgsConstructor
@RestController
@RequestMapping("/api/admin/analytics")
@Tag(name = "Analytics", description = "Кто до какого места курса дошёл. Только роль ADMIN")
@SecurityRequirement(name = "Bearer")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Прогресс всех клиентов по модулям",
            description = "Строка на клиента: ступень воронки, активность, и по каждому модулю — "
                    + "заходы, начатые и досмотренные уроки. Админы не входят. "
                    + "Сортировка и фильтры — на фронте, ответ отдаётся целиком")
    @GetMapping("/students")
    public ResponseEntity<AnalyticsDTO> getStudents() {
        return ResponseEntity.ok(analyticsService.getStudents());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("message", e.getReason() == null ? "Ошибка запроса" : e.getReason()));
    }
}
