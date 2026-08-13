package ru.anyforms.edu.service.auth.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.anyforms.edu.model.Role;
import ru.anyforms.edu.service.auth.JwtTokenService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
class JwtTokenServiceImpl implements JwtTokenService {

    private final SecretKey key;
    private final long expirationMs;

    JwtTokenServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration.seconds}") long expirationSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationSeconds * 1000L;
    }

    @Override
    public String createToken(String email, Role role, UUID sessionId) {
        var now = new Date();
        var expiry = new Date(now.getTime() + expirationMs);
        var builder = Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry);
        if (sessionId != null) {
            builder.claim("sid", sessionId.toString());
        }
        return builder.signWith(key).compact();
    }

    @Override
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parse(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    @Override
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public Role getRole(String token) {
        String roleStr = parseClaims(token).get("role", String.class);
        return roleStr != null ? Role.valueOf(roleStr) : null;
    }

    @Override
    public UUID getSessionId(String token) {
        String sid = parseClaims(token).get("sid", String.class);
        return sid == null ? null : UUID.fromString(sid);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
