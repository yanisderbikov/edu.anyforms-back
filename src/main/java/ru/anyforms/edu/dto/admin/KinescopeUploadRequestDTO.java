package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Запрос ссылки прямой загрузки видео урока в Kinescope. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KinescopeUploadRequestDTO {

    @NotBlank(message = "Не указано имя файла")
    private String filename;

    @NotNull(message = "Не указан размер файла")
    @Positive(message = "Размер файла должен быть больше нуля")
    private Long filesize;

    /** Название видео в кабинете Kinescope; пусто — возьмём имя файла. */
    private String title;
}
