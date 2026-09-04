package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.user.Student;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface StudentRepo extends JpaRepository<Student, UUID> {

    Optional<Student> findByEmail(String email);

    List<Student> findAllByOrderByCreatedAtDesc();

    List<Student> findByEmailContainingIgnoreCaseOrderByCreatedAtDesc(String emailPart);

    /**
     * Отметка активности. Условие по last_seen_at прямо в UPDATE: параллельные
     * запросы одного студента не перезапишут свежую отметку и не устроят гонку.
     */
    @Modifying
    @Query("UPDATE Student s SET s.lastSeenAt = :now, s.firstSeenAt = COALESCE(s.firstSeenAt, :now) "
            + "WHERE s.id = :id AND (s.lastSeenAt IS NULL OR s.lastSeenAt < :threshold)")
    int touchSeen(UUID id, Instant now, Instant threshold);
}
