package ru.anyforms.edu.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Обмен кода из письма на JWT. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCodeDTO {

    @NotBlank(message = "Не указан e-mail")
    @Email(message = "Некорректный e-mail")
    private String email;

    @NotBlank(message = "Не указан код")
    private String code;
}
