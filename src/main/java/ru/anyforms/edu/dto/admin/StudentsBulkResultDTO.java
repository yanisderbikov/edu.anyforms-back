package ru.anyforms.edu.dto.admin;

import java.util.List;

/**
 * Итог массового импорта.
 *
 * @param created  сколько аккаунтов заведено
 * @param existing email'ы, которые уже были в базе — их оставили как есть
 * @param invalid  строки, не похожие на email — их пропустили
 */
public record StudentsBulkResultDTO(int created, List<String> existing, List<String> invalid) {
}
