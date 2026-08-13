package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Запрос подписанного URL для прямой загрузки файла из браузера в S3. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignUploadRequestDTO {

    @NotBlank(message = "Не указано имя файла")
    private String filename;

    private String contentType;

    /** Папка в бакете: videos, modules… */
    private String prefix;
}
