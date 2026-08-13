package ru.anyforms.edu.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${url.back}")
    private String urlBack;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url(urlBack)))
                // Кнопка Authorize: вставить JWT из /api/public/auth/verify
                .components(new Components().addSecuritySchemes("Bearer",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT из ответа /api/public/auth/verify")))
                .info(new Info()
                        .title("edu.anyforms")
                        .description("API учебной платформы anyforms. Вход: запросить код на почту, обменять на JWT, нажать Authorize.")
                        .version("1.0.0"));
    }
}
