package ru.anyforms.edu.service.auth;

public interface AuthService {

    record AuthResult(String token, String role, String email) {
    }

    /** Отправляет код входа на почту (если email имеет доступ к платформе). */
    void requestCode(String email);

    /** Проверяет код и выдаёт JWT. Для STUDENT новый вход гасит старые токены. */
    AuthResult verify(String email, String code);
}
