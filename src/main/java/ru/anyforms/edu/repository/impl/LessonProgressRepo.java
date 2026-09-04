package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.anyforms.edu.model.user.LessonProgress;

import java.util.List;
import java.util.UUID;

@Repository
interface LessonProgressRepo extends JpaRepository<LessonProgress, Long> {

    List<LessonProgress> findByStudentIdAndCompletedAtIsNotNull(UUID studentId);

    /**
     * Первый запуск видео: строка появляется один раз, повторные вызовы ничего не меняют.
     * Upsert одним запросом — две вкладки или старт вместе с завершением не дадут дубля и ошибки.
     */
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO lesson_progress (student_id, lesson_id, started_at)
            VALUES (:studentId, :lessonId, now())
            ON CONFLICT (student_id, lesson_id) DO NOTHING
            """, nativeQuery = true)
    int insertStarted(@Param("studentId") UUID studentId, @Param("lessonId") UUID lessonId);

    /**
     * Урок досмотрен. Если старт не успел записаться (перемотка в конец сразу после play),
     * строка создаётся сразу завершённой; уже завершённый урок не трогаем.
     */
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO lesson_progress (student_id, lesson_id, started_at, completed_at)
            VALUES (:studentId, :lessonId, now(), now())
            ON CONFLICT (student_id, lesson_id)
                DO UPDATE SET completed_at = COALESCE(lesson_progress.completed_at, EXCLUDED.completed_at)
            """, nativeQuery = true)
    int upsertCompleted(@Param("studentId") UUID studentId, @Param("lessonId") UUID lessonId);
}
