package ru.anyforms.edu.dto.admin;

/**
 * Клиент курса.
 *
 * @param plan SELF / PERSONAL — тариф из anyforms-5
 * @param role ADMIN, если по этому email есть активная запись в service_user, иначе STUDENT
 */
public record StudentDTO(String id, String email, Boolean active, String plan, String role) {
}
