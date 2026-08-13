package ru.anyforms.edu.config.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CORSConfiguration;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;

import java.util.List;

/**
 * Прямая загрузка из браузера в S3 требует CORS на самом бакете
 * (allowed.origins в приложении отвечает только за ответы Spring).
 * Прописываем правила при старте, чтобы список доменов не разъезжался.
 * Отключается через s3.cors.auto=false.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class S3CorsConfigurer {

    private final ObjectProvider<S3Client> s3ClientProvider;
    private final S3Static s3Static;

    @Value("${allowed.origins}")
    private List<String> allowedOrigins;

    @Value("${s3.cors.auto:true}")
    private boolean autoConfigure;

    @EventListener(ApplicationReadyEvent.class)
    public void applyCors() {
        S3Client client = s3ClientProvider.getIfAvailable();
        if (!autoConfigure || client == null) {
            return;
        }
        CORSRule rule = CORSRule.builder()
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "PUT", "HEAD")
                .allowedHeaders("*")
                .exposeHeaders("ETag")
                .maxAgeSeconds(3600)
                .build();
        try {
            client.putBucketCors(PutBucketCorsRequest.builder()
                    .bucket(s3Static.getBucketName())
                    .corsConfiguration(CORSConfiguration.builder().corsRules(rule).build())
                    .build());
            log.info("CORS бакета {} обновлён для origins: {}", s3Static.getBucketName(), allowedOrigins);
        } catch (Exception e) {
            log.warn("Не удалось настроить CORS бакета {} ({}). Пропишите правила вручную в консоли Yandex Cloud: "
                            + "origins={}, methods=GET,PUT,HEAD, headers=*",
                    s3Static.getBucketName(), e.getMessage(), allowedOrigins);
        }
    }
}
