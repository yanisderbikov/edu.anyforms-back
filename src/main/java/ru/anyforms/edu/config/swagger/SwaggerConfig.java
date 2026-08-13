package ru.anyforms.edu.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
                .info(new Info()
                        .title("edu.anyforms")
                        .description("API учебной платформы anyforms")
                        .version("1.0.0"));
    }
}
