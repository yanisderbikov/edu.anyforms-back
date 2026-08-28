package ru.anyforms.edu.dto.auth;

/**
 * Результат входа.
 *
 * @param token JWT на месяц (Bearer)
 * @param role  ADMIN или STUDENT
 */
public record AuthResponseDTO(String token, String role, String email) {
}
