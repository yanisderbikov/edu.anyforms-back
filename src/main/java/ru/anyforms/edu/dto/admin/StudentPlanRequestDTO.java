package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Формат обучения: SELF — общий, PERSONAL — персональный.
 * У клиентов с покупкой тариф перезапишется из anyforms при следующем входе.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPlanRequestDTO {

    @NotBlank(message = "Не указан формат")
    @Pattern(regexp = "SELF|PERSONAL", message = "Формат должен быть SELF или PERSONAL")
    private String plan;
}
