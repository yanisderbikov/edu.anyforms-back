package ru.anyforms.edu.model.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Урок в работе у клиента: одна строка на пару студент+урок.
 * Появляется при первом запуске видео (started_at), completed_at проставляется,
 * когда урок досмотрен. Пишется upsert'ами (см. LessonProgressRepo), поэтому
 * гонка «старт и завершение одновременно» не ломает данные.
 */
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

    /** Первый запуск видео урока */
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    /** Досмотрел (90% или до конца); NULL = начал, но не досмотрел */
    @Column(name = "completed_at")
    private Instant completedAt;

    public boolean isCompleted() {
        return completedAt != null;
    }
}
