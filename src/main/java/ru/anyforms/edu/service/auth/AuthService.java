package ru.anyforms.edu.service.auth;

import ru.anyforms.edu.dto.auth.AuthResponseDTO;

public interface AuthService {

    /** Отправляет код входа на почту (если email имеет доступ к платформе). */
    void requestCode(String email);

    /** Проверяет код и выдаёт JWT. Для STUDENT новый вход гасит старые токены. */
    AuthResponseDTO verify(String email, String code);
}
