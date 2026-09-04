package ru.anyforms.edu.model.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Модуль курса. Открыт, если opensAt == null или момент открытия уже наступил. */
@Entity
@Table(name = "course_module")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseModule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Integer ord;

    @Column(nullable = false)
    private String title;

    /** Превью карточки на главном экране */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Текст под вводным видео на странице модуля (без видео — сразу под заголовком) */
    @Column(name = "video_description", columnDefinition = "TEXT")
    private String videoDescription;

    /** Картинка карточки (16:9): ключ S3 или полный URL */
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    /** Обложка страницы модуля (широкий баннер): ключ S3 или полный URL */
    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    /** Вводное видео модуля: embed-ссылка Kinescope или ключ S3 */
    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    /** Обложка видео модуля (постер до запуска): ключ S3 или полный URL */
    @Column(name = "video_cover_url", columnDefinition = "TEXT")
    private String videoCoverUrl;

    /** NULL = открыт; будущий момент = «Откроется N числа в ЧЧ:ММ». Задаётся в МСК, см. MskTime */
    @Column(name = "opens_at")
    private Instant opensAt;

    /** Когда поставили в очередь письма «модуль открыт»; NULL = об открытии ещё не объявляли */
    @Column(name = "open_email_queued_at")
    private Instant openEmailQueuedAt;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
    @OrderBy("ord ASC")
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isOpen() {
        return opensAt == null || !opensAt.isAfter(Instant.now());
    }
}
