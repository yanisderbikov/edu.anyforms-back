package ru.anyforms.edu.dto.admin;

/**
 * Клиент курса.
 *
 * @param plan SELF / PERSONAL — тариф из anyforms-5
 */
public record StudentDTO(String id, String email, Boolean active, String plan) {
}
