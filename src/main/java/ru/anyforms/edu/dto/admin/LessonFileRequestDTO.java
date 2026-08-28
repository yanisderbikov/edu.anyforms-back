package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Прикрепление файла к уроку: сам файл уже в S3 (см. presign-upload). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonFileRequestDTO {

    /** Исходное имя файла — под ним студент его скачает */
    @NotBlank(message = "Не указано имя файла")
    private String name;

    /** Ключ файла в S3 или полный URL */
    @NotBlank(message = "Не указан файл")
    private String fileUrl;

    /** Размер в байтах — для подписи в списке материалов */
    private Long sizeBytes;
}
