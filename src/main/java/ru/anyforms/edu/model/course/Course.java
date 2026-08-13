package ru.anyforms.edu.model.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Курс. Пока один (molds-course), но схема готова к нескольким. */
@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    public static final String DEFAULT_SLUG = "molds-course";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String subtitle;

    @Builder.Default
    @Column(name = "chat_label", nullable = false, length = 64)
    private String chatLabel = "Чат курса";

    @Column(name = "chat_url", columnDefinition = "TEXT")
    private String chatUrl;

    @Builder.Default
    @Column(name = "support_label", nullable = false, length = 64)
    private String supportLabel = "Поддержка";

    @Column(name = "support_url", columnDefinition = "TEXT")
    private String supportUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @OrderBy("ord ASC")
    @Builder.Default
    private List<CourseModule> modules = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
