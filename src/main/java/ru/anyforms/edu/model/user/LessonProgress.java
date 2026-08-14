package ru.anyforms.edu.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Полностью просмотренный урок клиента. */
@Entity
@Table(name = "lesson_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false, updatable = false)
    private Instant completedAt;
}
