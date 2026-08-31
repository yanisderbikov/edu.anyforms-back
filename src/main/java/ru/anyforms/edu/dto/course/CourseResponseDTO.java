package ru.anyforms.edu.dto.course;

import java.util.List;

/**
 * Ответ /api/course. Список модулей идёт без уроков — только превью со счётчиками,
 * чтобы главная не тянула весь курс; уроки приходят из /api/course/modules/{id}.
 */
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
            /** Подписанная ссылка на обложку страницы модуля (широкий баннер) */
            String cover,
            /** Сырое значение из БД — только в админском ответе */
            String coverKey,
            /** Вводное видео модуля; студенту отдаётся только у открытого */
            String videoUrl,
            /** Сырое значение из БД — только в админском ответе */
            String videoKey,
            /** Подписанная ссылка на обложку видео (постер до запуска) */
            String videoCover,
            /** Сырое значение из БД — только в админском ответе */
            String videoCoverKey,
            String status,
            /** Московское время открытия «2026-09-01T14:00»; null = открыт сразу */
            String opensAt,
            /** Сколько уроков в модуле — для карточки на главной */
            int lessonsCount,
            /** Сколько из них досмотрел этот студент (у админа всегда 0) */
            int lessonsDone,
            /** Уроки: пусто в списке модулей, заполнено при запросе одного модуля и в админке */
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
