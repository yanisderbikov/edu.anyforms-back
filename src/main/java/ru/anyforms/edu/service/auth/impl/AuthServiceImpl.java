package ru.anyforms.edu.service.auth.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.model.Role;
import ru.anyforms.edu.model.user.LoginCode;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.repository.GetterServiceUser;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.repository.LoginCodeStore;
import ru.anyforms.edu.repository.SaverStudent;
import ru.anyforms.edu.service.auth.AuthService;
import ru.anyforms.edu.service.auth.JwtTokenService;
import ru.anyforms.edu.service.email.EmailService;
import ru.anyforms.edu.service.email.EmailTemplate;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
class AuthServiceImpl implements AuthService {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    /** Повторный код не чаще раза в минуту */
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GetterServiceUser getterServiceUser;
    private final GetterStudent getterStudent;
    private final SaverStudent saverStudent;
    private final LoginCodeStore loginCodeStore;
    private final EmailService emailService;
    private final JwtTokenService jwtTokenService;

    /** ADMIN приоритетнее: если email есть и там и там, входит как админ. */
    private Role resolveRole(String email) {
        var admin = getterServiceUser.getByEmail(email);
        if (admin.isPresent() && Boolean.TRUE.equals(admin.get().getActive())) {
            return Role.ADMIN;
        }
        var student = getterStudent.getByEmail(email);
        if (student.isPresent() && Boolean.TRUE.equals(student.get().getActive())) {
            return Role.STUDENT;
        }
        return null;
    }

    @Override
    @Transactional
    public void requestCode(String rawEmail) {
        String email = ServiceUser.normalizeEmail(rawEmail);
        if (resolveRole(email) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "У этого e-mail нет доступа к курсу. Проверьте адрес или напишите в поддержку.");
        }

        var existing = loginCodeStore.getLatestActive(email);
        if (existing.isPresent() && !existing.get().isExpired()
                && existing.get().getCreatedAt() != null
                && existing.get().getCreatedAt().isAfter(Instant.now().minus(RESEND_COOLDOWN))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Код уже отправлен. Проверьте почту или попробуйте через минуту.");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        loginCodeStore.invalidateAll(email);
        loginCodeStore.save(LoginCode.builder()
                .email(email)
                .code(code)
                .expiresAt(Instant.now().plus(CODE_TTL))
                .build());

        emailService.sendEmail(email, "Код для входа: " + code, EmailTemplate.getLoginCodeEmail(code));
        log.info("Код входа отправлен на {}", email);
    }

    @Override
    @Transactional
    public AuthResult verify(String rawEmail, String rawCode) {
        String email = ServiceUser.normalizeEmail(rawEmail);
        String code = rawCode == null ? "" : rawCode.trim();

        LoginCode loginCode = loginCodeStore.getLatestActive(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Код не найден. Запросите новый."));

        if (loginCode.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Код истёк. Запросите новый.");
        }
        if (loginCode.getAttempts() >= LoginCode.MAX_ATTEMPTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Слишком много попыток. Запросите новый код.");
        }
        if (!loginCode.getCode().equals(code)) {
            loginCode.setAttempts(loginCode.getAttempts() + 1);
            loginCodeStore.save(loginCode);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный код. Проверьте письмо.");
        }

        loginCode.setUsed(true);
        loginCodeStore.save(loginCode);

        Role role = resolveRole(email);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "У этого e-mail нет доступа к курсу.");
        }

        UUID sessionId = null;
        if (role == Role.STUDENT) {
            // «Одно устройство»: новый вход выпускает новую сессию, старые JWT гаснут
            Student student = getterStudent.getByEmail(email).orElseThrow();
            sessionId = UUID.randomUUID();
            student.setCurrentSessionId(sessionId);
            saverStudent.save(student);
        }

        String token = jwtTokenService.createToken(email, role, sessionId);
        return new AuthResult(token, role.name(), email);
    }
}
