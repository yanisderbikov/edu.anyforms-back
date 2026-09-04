package ru.anyforms.edu.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.anyforms.edu.model.Role;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.repository.SaverStudent;
import ru.anyforms.edu.service.auth.JwtTokenService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final GetterStudent getterStudent;
    private final SaverStudent saverStudent;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && jwtTokenService.isValid(token)) {
            String email = jwtTokenService.getEmail(token);
            Role role = jwtTokenService.getRole(token);
            if (role != null && sessionAlive(email, role, jwtTokenService.getSessionId(token))) {
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role.name()));
                var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * «Одно устройство» для студентов: токен живёт, пока его sid совпадает
     * с current_session_id. Новый вход меняет sid — старый токен гаснет.
     * На админов не распространяется.
     * Заодно отмечаем активность: запрос с живым токеном = студент на платформе.
     */
    private boolean sessionAlive(String email, Role role, UUID sid) {
        if (role != Role.STUDENT) {
            return true;
        }
        if (sid == null) {
            return false;
        }
        Student student = getterStudent.getByEmail(email).orElse(null);
        if (student == null
                || !Boolean.TRUE.equals(student.getActive())
                || !sid.equals(student.getCurrentSessionId())) {
            return false;
        }
        touchSeen(student);
        return true;
    }

    /** Аналитика — не повод ронять запрос: сбой отметки логируем и идём дальше */
    private void touchSeen(Student student) {
        try {
            saverStudent.touchSeen(student);
        } catch (Exception e) {
            log.warn("Не удалось отметить активность {}", student.getEmail(), e);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (StringUtils.hasText(header) && header.startsWith(PREFIX)) {
            return header.substring(PREFIX.length());
        }
        return null;
    }
}
