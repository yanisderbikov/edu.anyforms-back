package ru.anyforms.edu.integration.impl;

import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import ru.anyforms.edu.integration.CourseAccessClient;

import java.time.Duration;

/**
 * Клиент к anyforms-5: GET /api/tech/course-access?email=…
 * Авторизация — общий межсервисный токен в заголовке X-Auth-Token.
 * Без настроек приложение не поднимется: без этой проверки клиенты не смогут войти.
 */
@Component
@Slf4j
class AnyformsCourseAccessClient implements CourseAccessClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(8);

    private final WebClient webClient;
    private final String serviceToken;

    AnyformsCourseAccessClient(@Value("${anyforms.api.url}") String baseUrl,
                               @Value("${anyforms.service.jwt.token}") String serviceToken) {
        require(baseUrl, "ANYFORMS_API_URL");
        require(serviceToken, "SERVICE_JWT_TOKEN");

        this.serviceToken = serviceToken;
        HttpClient http = HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE);
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(http))
                .baseUrl(baseUrl)
                .build();
    }

    private static void require(String value, String envName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Не задан " + envName
                    + " — без него нельзя проверить доступ клиентов к курсу в anyforms-5");
        }
    }

    @Override
    public CourseAccess check(String email) {
        try {
            AccessResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/tech/course-access")
                            .queryParam("email", email)
                            .build())
                    .header("X-Auth-Token", serviceToken)
                    .retrieve()
                    .bodyToMono(AccessResponse.class)
                    .block(TIMEOUT);

            if (response == null) {
                throw new CourseAccessUnavailableException("anyforms-5 вернул пустой ответ", null);
            }
            log.info("Доступ к курсу для {}: hasAccess={}, plan={}", email, response.hasAccess(), response.plan());
            return new CourseAccess(response.hasAccess(), response.plan());
        } catch (CourseAccessUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Не удалось проверить доступ к курсу в anyforms-5 для {}", email, e);
            throw new CourseAccessUnavailableException("anyforms-5 недоступен", e);
        }
    }

    private record AccessResponse(boolean hasAccess, String plan, String productCode) {
    }
}
