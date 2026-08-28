package ru.anyforms.edu.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Запрос кода входа на почту. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCodeDTO {

    @NotBlank(message = "Не указан e-mail")
    @Email(message = "Некорректный e-mail")
    private String email;
}
