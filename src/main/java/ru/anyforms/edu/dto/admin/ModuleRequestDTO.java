package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Создание/обновление модуля из админки. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleRequestDTO {

    @NotNull(message = "Не указан порядок модуля")
    private Integer order;

    @NotBlank(message = "Не указано название модуля")
    private String title;

    /** Превью карточки на главном экране */
    private String description;

    /** Текст под вводным видео на странице модуля */
    private String videoDescription;

    /** Картинка карточки (16:9): ключ S3 или полный URL */
    private String imageUrl;

    /** Обложка страницы модуля (широкий баннер): ключ S3 или полный URL */
    private String coverUrl;

    /** Вводное видео модуля: embed-ссылка Kinescope или ключ S3 */
    private String videoUrl;

    /** Обложка видео модуля (постер до запуска): ключ S3 или полный URL */
    private String videoCoverUrl;

    /** Заголовок над кнопкой обратной связи в конце модуля; пусто = текст по умолчанию */
    private String feedbackTitle;

    /** Текст под заголовком блока обратной связи; пусто = без текста */
    private String feedbackDescription;

    /** Подпись на кнопке обратной связи; пусто = текст по умолчанию */
    private String feedbackLabel;

    /** Ссылка кнопки обратной связи; пусто = блок не показывается */
    private String feedbackUrl;

    /** Московское время открытия, «2026-09-01T14:00»; null = модуль открыт */
    private LocalDateTime opensAt;
}
