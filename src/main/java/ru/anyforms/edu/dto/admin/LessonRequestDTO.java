package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Создание/обновление урока из админки. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRequestDTO {

    @NotNull(message = "Не указан порядок урока")
    private Integer order;

    @NotBlank(message = "Не указано название урока")
    private String title;

    private String description;

    /** Ключ видео в S3 или полный URL */
    private String videoUrl;
}
