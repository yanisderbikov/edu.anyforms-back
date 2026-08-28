package ru.anyforms.edu.model.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Урок: заголовок + видео + описание + файлы-материалы.
 * Удаление мягкое: строка с проставленным deleted_at остаётся в базе
 * (иначе каскадом пропал бы прогресс студентов), но во всех выборках
 * Hibernate её отсекает — включая коллекцию уроков модуля.
 */
@Entity
@Table(name = "lesson")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(nullable = false)
    private Integer ord;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Ключ видео в S3 или полный URL */
    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    /** Обложка урока (16:9): ключ S3 или полный URL */
    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    /** Файлы-материалы в порядке добавления */
    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC, id ASC")
    @Builder.Default
    private List<LessonFile> files = new ArrayList<>();

    /** Проставлен — урок удалён; файлы из S3 и Kinescope при этом уже стёрты */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
