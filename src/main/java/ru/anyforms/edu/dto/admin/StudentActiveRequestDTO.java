package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Включение/отключение доступа клиента. Отключённого не реактивирует даже покупка. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentActiveRequestDTO {

    @NotNull(message = "Не указан статус active")
    private Boolean active;
}
