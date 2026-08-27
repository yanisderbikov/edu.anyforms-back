package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Назначение прав: ADMIN заводит запись в service_user, STUDENT — гасит её. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRoleRequestDTO {

    @NotBlank(message = "Не указана роль")
    @Pattern(regexp = "ADMIN|STUDENT", message = "Роль должна быть ADMIN или STUDENT")
    private String role;
}
