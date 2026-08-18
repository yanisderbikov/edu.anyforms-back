package ru.anyforms.edu.model.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Файл-материал урока: методичка, чек-лист, исходник. Количество не ограничено. */
@Entity
@Table(name = "lesson_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    /** Исходное имя файла — под ним студент его скачает */
    @Column(nullable = false)
    private String name;

    /** Ключ файла в S3 или полный URL */
    @Column(name = "file_url", columnDefinition = "TEXT", nullable = false)
    private String fileUrl;

    /** Размер в байтах — для подписи в списке материалов */
    @Column(name = "size_bytes")
    private Long sizeBytes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
