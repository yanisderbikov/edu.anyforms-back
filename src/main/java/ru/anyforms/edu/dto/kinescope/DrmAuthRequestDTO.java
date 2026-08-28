package ru.anyforms.edu.dto.kinescope;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Тело колбэка авторизации от Kinescope: кто и какое видео пытается смотреть.
 * Для решения нам нужен только token (наш же токен воспроизведения),
 * остальное — контекст для логов.
 */
public record DrmAuthRequestDTO(String id,
                                String type,
                                String token,
                                String ip,
                                @JsonProperty("user_agent") String userAgent) {
}
