package ru.anyforms.edu.dto.course;

import java.util.List;

/** Полный ответ /api/course — ровно тот JSON, который ждёт фронтенд. */
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
            /** Подписанная ссылка на картинку карточки (16:9) */
            String image,
            /** Сырое значение из БД — только в админском ответе */
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
            String videoKey,
            /** Подписанная ссылка на обложку урока (16:9) */
            String cover,
            /** Сырое значение из БД — только в админском ответе */
            String coverKey,
            /** Файлы-материалы урока в порядке добавления */
            List<LessonFileDTO> files
    ) {
    }

    public record LessonFileDTO(
            String id,
            /** Имя, под которым файл скачается */
            String name,
            /** Подписанная ссылка на скачивание */
            String url,
            /** Размер в байтах, может быть null */
            Long sizeBytes
    ) {
    }
}
