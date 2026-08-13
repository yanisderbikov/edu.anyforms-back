package ru.anyforms.edu.service.auth;

import ru.anyforms.edu.model.Role;

import java.util.UUID;

public interface JwtTokenService {

    /** sessionId — «одно устройство» для STUDENT; у ADMIN может быть null. */
    String createToken(String email, Role role, UUID sessionId);

    boolean isValid(String token);

    String getEmail(String token);

    Role getRole(String token);

    UUID getSessionId(String token);
}
