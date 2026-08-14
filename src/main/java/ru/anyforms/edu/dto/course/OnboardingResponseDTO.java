package ru.anyforms.edu.dto.course;

import java.util.List;

/** Слайды онбординга + ссылки поддержки (для слайда с kind = SUPPORT). */
public record OnboardingResponseDTO(
        List<SlideDTO> slides,
        CourseResponseDTO.SupportDTO support
) {

    public record SlideDTO(
            String id,
            int order,
            String kind,
            String eyebrow,
            /** Слово в {фигурных скобках} фронт красит акцентом */
            String title,
            String body,
            List<String> points,
            String image,
            /** Сырое значение из БД (ключ S3 или URL) — только в админском ответе */
            String imageKey
    ) {
    }
}
