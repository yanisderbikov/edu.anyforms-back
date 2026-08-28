package ru.anyforms.edu.service.auth.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.auth.AuthResponseDTO;
import ru.anyforms.edu.integration.CourseAccessClient;
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
    /** Повторный код не чаще раза в 30 секунд */
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GetterServiceUser getterServiceUser;
    private final GetterStudent getterStudent;
    private final SaverStudent saverStudent;
    private final LoginCodeStore loginCodeStore;
    private final EmailService emailService;
    private final JwtTokenService jwtTokenService;
    private final CourseAccessClient courseAccessClient;

    private boolean isAdmin(String email) {
        return getterServiceUser.getByEmail(email)
                .map(u -> Boolean.TRUE.equals(u.getActive()))
                .orElse(false);
    }

    /** ADMIN приоритетнее: если email есть и там и там, входит как админ. */
    private Role resolveRole(String email) {
        if (isAdmin(email)) {
            return Role.ADMIN;
        }
        var student = getterStudent.getByEmail(email);
        if (student.isPresent() && Boolean.TRUE.equals(student.get().getActive())) {
            return Role.STUDENT;
        }
        return null;
    }

    /**
     * Порядок проверки: сначала своя база, потом anyforms-back.
     * Знакомый студент с активным доступом (уже заходил или добавлен админом)
     * входит без похода в anyforms-back; деактивированный админом — не входит,
     * даже если покупка есть. И только незнакомый email проверяем в anyforms-back
     * (там лежат оплаченные покупки курса) — ответ сохраняем у себя как право
     * входа и тариф (SELF / PERSONAL), дальше он живёт в нашей базе.
     */
    private void syncStudentAccess(String email) {
        Student student = getterStudent.getByEmail(email).orElse(null);
        if (student != null) {
            if (Boolean.TRUE.equals(student.getActive())) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Доступ к курсу отключён. Напишите в поддержку.");
        }

        CourseAccessClient.CourseAccess access;
        try {
            access = courseAccessClient.check(email);
        } catch (CourseAccessClient.CourseAccessUnavailableException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Не получилось проверить доступ. Попробуйте через минуту или напишите в поддержку.");
        }

        if (!access.hasAccess()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "У этого e-mail нет доступа к курсу. Проверьте адрес или напишите в поддержку.");
        }

        saverStudent.save(Student.builder()
                .email(email)
                .plan(access.plan())
                .active(Boolean.TRUE)
                .build());
    }

    @Override
    @Transactional
    public void requestCode(String rawEmail) {
        String email = ServiceUser.normalizeEmail(rawEmail);
        if (!isAdmin(email)) {
            syncStudentAccess(email);
        }

        var existing = loginCodeStore.getLatestActive(email);
        if (existing.isPresent() && !existing.get().isExpired()
                && existing.get().getCreatedAt() != null
                && existing.get().getCreatedAt().isAfter(Instant.now().minus(RESEND_COOLDOWN))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Код уже отправлен. Проверьте почту или запросите новый через 30 секунд.");
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
    public AuthResponseDTO verify(String rawEmail, String rawCode) {
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
        return new AuthResponseDTO(token, role.name(), email);
    }
}
