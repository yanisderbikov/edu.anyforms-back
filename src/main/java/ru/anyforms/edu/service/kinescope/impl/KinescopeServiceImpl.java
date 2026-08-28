package ru.anyforms.edu.service.kinescope.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.netty.http.client.HttpClient;
import ru.anyforms.edu.repository.GetterServiceUser;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.service.kinescope.KinescopeService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Service
@Slf4j
class KinescopeServiceImpl implements KinescopeService {

    private static final String UPLOADER_URL = "https://uploader.kinescope.io";
    private static final String API_URL = "https://api.kinescope.io";
    private static final String EMBED_URL = "https://kinescope.io/embed/";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    /**
     * Метка токена воспроизведения. Подписаны тем же jwt.secret, что и сессии,
     * но семейства не взаимозаменяемы: у сессионного нет purpose (сюда не пройдёт),
     * у этого нет role (JwtAuthFilter его не пустит в API).
     */
    private static final String PURPOSE = "kinescope-drm";

    private final WebClient webClient;
    private final WebClient apiWebClient;
    private final String apiToken;
    private final String parentId;
    private final SecretKey jwtKey;
    private final long videoTokenTtlSeconds;
    private final GetterStudent getterStudent;
    private final GetterServiceUser getterServiceUser;

    KinescopeServiceImpl(@Value("${kinescope.api-token}") String apiToken,
                         @Value("${kinescope.parent-id}") String parentId,
                         @Value("${kinescope.video-token.ttl-seconds}") long videoTokenTtlSeconds,
                         @Value("${jwt.secret}") String jwtSecret,
                         GetterStudent getterStudent,
                         GetterServiceUser getterServiceUser) {
        this.apiToken = apiToken == null ? "" : apiToken.trim();
        this.parentId = parentId == null ? "" : parentId.trim();
        this.videoTokenTtlSeconds = videoTokenTtlSeconds;
        this.jwtKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.getterStudent = getterStudent;
        this.getterServiceUser = getterServiceUser;
        HttpClient http = HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE);
        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(http));
        this.webClient = builder.clone().baseUrl(UPLOADER_URL).build();
        this.apiWebClient = builder.clone().baseUrl(API_URL).build();
    }

    @Override
    public UploadLink createUploadLink(String filename, long filesize, String title) {
        if (apiToken.isBlank() || parentId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Kinescope не настроен: заполните KINESCOPE_API_TOKEN и KINESCOPE_PARENT_ID");
        }
        InitResponse response;
        try {
            response = webClient.post()
                    .uri("/v2/init")
                    .header("Authorization", "Bearer " + apiToken)
                    .bodyValue(new InitRequest("video", parentId, title, filename, filesize))
                    .retrieve()
                    .bodyToMono(InitResponse.class)
                    .block(TIMEOUT);
        } catch (Exception e) {
            log.error("Kinescope: не удалось создать ссылку загрузки для «{}»", filename, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Kinescope недоступен или отверг запрос — попробуйте ещё раз");
        }
        if (response == null || response.data() == null
                || response.data().id() == null || response.data().endpoint() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Kinescope вернул неожиданный ответ на запрос загрузки");
        }
        return new UploadLink(response.data().id(), response.data().endpoint(),
                EMBED_URL + response.data().id());
    }

    @Override
    public VideoToken createVideoToken(String email) {
        var now = new Date();
        var expiry = new Date(now.getTime() + videoTokenTtlSeconds * 1000L);
        String token = Jwts.builder()
                .subject(email)
                .claim("purpose", PURPOSE)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(jwtKey)
                .compact();
        return new VideoToken(token, videoTokenTtlSeconds);
    }

    @Override
    public boolean authorizePlayback(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(jwtKey).build().parseSignedClaims(token).getPayload();
        } catch (JwtException e) {
            log.warn("Kinescope drm-auth: невалидный или истёкший токен: {}", e.getMessage());
            return false;
        }
        if (!PURPOSE.equals(claims.get("purpose", String.class))) {
            log.warn("Kinescope drm-auth: токен без метки purpose — похоже на подмену");
            return false;
        }
        String email = claims.getSubject();
        if (email == null || email.isBlank()) {
            return false;
        }
        // Студент должен быть жив и не отключён; админы — из service_user
        boolean allowed = getterStudent.getByEmail(email)
                .map(s -> Boolean.TRUE.equals(s.getActive()))
                .orElseGet(() -> getterServiceUser.getByEmail(email).isPresent());
        if (!allowed) {
            log.warn("Kinescope drm-auth: доступ запрещён для {}", email);
        }
        return allowed;
    }

    @Override
    public void deleteVideo(String videoId) {
        if (apiToken.isBlank()) {
            log.warn("Kinescope не настроен — видео {} осталось в кабинете", videoId);
            return;
        }
        try {
            apiWebClient.delete()
                    .uri("/v1/videos/{id}", videoId)
                    .header("Authorization", "Bearer " + apiToken)
                    .retrieve()
                    .toBodilessEntity()
                    .block(TIMEOUT);
            log.info("Kinescope: видео {} удалено", videoId);
        } catch (WebClientResponseException.NotFound e) {
            // Уже удалено вручную в кабинете — считаем задачу выполненной
            log.info("Kinescope: видео {} в кабинете не найдено, пропускаем", videoId);
        }
    }

    private record InitRequest(String type,
                               @JsonProperty("parent_id") String parentId,
                               String title,
                               String filename,
                               long filesize) {
    }

    private record InitResponse(InitData data) {
    }

    private record InitData(String id, String endpoint) {
    }
}
