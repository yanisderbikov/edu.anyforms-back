package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    private String description;

    /** null = модуль открыт */
    private LocalDate opensAt;
}
