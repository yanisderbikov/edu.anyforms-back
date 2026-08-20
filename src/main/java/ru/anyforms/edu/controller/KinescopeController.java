package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.anyforms.edu.dto.kinescope.DrmAuthRequestDTO;
import ru.anyforms.edu.service.kinescope.KinescopeService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Kinescope: токен воспроизведения для плеера и колбэк DRM-авторизации.
 * Ссылку загрузки для админки выдаёт AdminCourseController (/api/admin/kinescope/…).
 */
@RestController
@Tag(name = "Kinescope", description = "Токены воспроизведения видео и колбэк авторизации Kinescope")
public class KinescopeController {

    private final KinescopeService kinescopeService;
    private final String callbackUser;
    private final String callbackPassword;

    KinescopeController(KinescopeService kinescopeService,
                        @Value("${kinescope.drm.callback-user}") String callbackUser,
                        @Value("${kinescope.drm.callback-password}") String callbackPassword) {
        this.kinescopeService = kinescopeService;
        this.callbackUser = callbackUser == null ? "" : callbackUser.trim();
        this.callbackPassword = callbackPassword == null ? "" : callbackPassword;
    }

    @Operation(summary = "Токен воспроизведения видео",
            description = "Плеер передаёт его Kinescope (drmauthtoken); перед выдачей ключей "
                    + "Kinescope возвращает токен нам в POST /api/kinescope/drm-auth на проверку")
    @SecurityRequirement(name = "Bearer")
    @GetMapping("/api/course/video-token")
    public ResponseEntity<Map<String, Object>> videoToken(Authentication authentication) {
        KinescopeService.VideoToken token = kinescopeService.createVideoToken(authentication.getName());
        return ResponseEntity.ok(Map.of(
                "token", token.token(),
                "expiresInSeconds", token.expiresInSeconds()));
    }

    @Operation(summary = "Колбэк авторизации от Kinescope",
            description = "Kinescope спрашивает, пускать ли зрителя: 200 — играть, 403 — нет. "
                    + "URL регистрируется у Kinescope через PUT https://api.kinescope.io/v1/drm/auth")
    @PostMapping("/api/kinescope/drm-auth")
    public ResponseEntity<Void> drmAuth(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody DrmAuthRequestDTO request) {
        if (!basicAuthOk(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return kinescopeService.authorizePlayback(request.token())
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /** Если задан kinescope.drm.callback-user — колбэк обязан прийти с этой Basic-парой. */
    private boolean basicAuthOk(String header) {
        if (callbackUser.isBlank()) {
            return true;
        }
        String expected = "Basic " + Base64.getEncoder().encodeToString(
                (callbackUser + ":" + callbackPassword).getBytes(StandardCharsets.UTF_8));
        return expected.equals(header);
    }
}
