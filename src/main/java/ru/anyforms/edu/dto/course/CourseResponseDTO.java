package ru.anyforms.edu.dto.course;

import java.util.List;

/** Полный ответ /api/public/course — ровно тот JSON, который ждёт фронтенд. */
public record CourseResponseDTO(
        CourseDTO course,
        SupportDTO support,
        List<ModuleDTO> modules
) {

    public record CourseDTO(String id, String title, String subtitle, int modulesCount) {
    }

    public record SupportDTO(String chatLabel, String chatUrl, String supportLabel, String supportUrl) {
    }

    public record ModuleDTO(
            String id,
            int order,
            String title,
            String description,
            List<String> points,
            String image,
            /** Сырое значение из БД (ключ S3 или URL) — только в админском ответе */
            String imageKey,
            String status,
            String opensAt,
            List<LessonDTO> lessons
    ) {
    }

    public record LessonDTO(
            String id,
            String title,
            String description,
            String videoUrl,
            /** Сырое значение из БД — только в админском ответе */
            String videoKey
    ) {
    }
}
