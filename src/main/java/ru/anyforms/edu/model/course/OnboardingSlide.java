package ru.anyforms.edu.model.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Слайд онбординга. Независим от модулей курса: правится в /admin/onboarding.
 * В title слово в {фигурных скобках} выделяется акцентом.
 */
@Entity
@Table(name = "onboarding_slide")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingSlide {

    /** Обычный слайд */
    public static final String KIND_TEXT = "TEXT";
    /** Со ссылками на чат и поддержку */
    public static final String KIND_SUPPORT = "SUPPORT";
    /** Последний — кнопка «Поехали!» */
    public static final String KIND_FINAL = "FINAL";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Integer ord;

    @Builder.Default
    @Column(nullable = false, length = 16)
    private String kind = KIND_TEXT;

    private String eyebrow;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    /** Пункты со стрелками — по одному на строку */
    @Column(columnDefinition = "TEXT")
    private String points;

    /** Ключ картинки в S3 или полный URL */
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public List<String> pointsList() {
        if (points == null || points.isBlank()) return List.of();
        return Arrays.stream(points.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
