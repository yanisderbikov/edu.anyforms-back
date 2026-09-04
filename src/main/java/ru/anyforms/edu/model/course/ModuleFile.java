package ru.anyforms.edu.model.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Файл-материал модуля: то же, что материал урока, но лежит под описанием модуля.
 * Модуль удаляется жёстко, поэтому его файлы уходят каскадом вместе с ним.
 */
@Entity
@Table(name = "module_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

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
