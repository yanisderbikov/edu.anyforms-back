package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Добавление сервисного пользователя (админа). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceUserRequestDTO {

    @NotBlank(message = "Не указан email")
    @Email(message = "Некорректный email")
    private String email;

    private String role;
}
