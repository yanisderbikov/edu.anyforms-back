package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Создание/обновление слайда онбординга. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlideRequestDTO {

    @NotNull(message = "Не указан порядок слайда")
    private Integer order;

    /** TEXT (обычный), SUPPORT (ссылки чат/поддержка), FINAL (последний, «Поехали!») */
    private String kind;

    private String eyebrow;

    /** Может быть пустым: новый слайд создаётся без заголовка и заполняется после */
    private String title;

    private String body;

    private List<String> points;

    /** Ключ картинки в S3 или полный URL */
    private String imageUrl;
}
