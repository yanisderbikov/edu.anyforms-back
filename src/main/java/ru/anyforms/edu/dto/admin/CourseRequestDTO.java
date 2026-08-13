package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Обновление шапки курса из админки. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequestDTO {

    @NotBlank(message = "Не указано название курса")
    private String title;

    private String subtitle;

    private String chatLabel;

    private String chatUrl;

    private String supportLabel;

    private String supportUrl;
}
