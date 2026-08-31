package ru.anyforms.edu.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Массовая выдача доступа: список email из таблицы одним запросом.
 * Уже существующих не трогаем — иначе импорт воскресил бы отключённые вручную аккаунты.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentsBulkRequestDTO {

    @NotEmpty(message = "Список email пуст")
    @Size(max = 2000, message = "За раз можно добавить не больше 2000 email")
    private List<String> emails;

    /** Пусто = SELF: у купивших тариф всё равно придёт из anyforms при входе */
    @Pattern(regexp = "SELF|PERSONAL", message = "Формат должен быть SELF или PERSONAL")
    private String plan;
}
