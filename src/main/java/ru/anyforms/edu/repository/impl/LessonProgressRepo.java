package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.user.LessonProgress;

import java.util.List;
import java.util.UUID;

@Repository
interface LessonProgressRepo extends JpaRepository<LessonProgress, Long> {

    List<LessonProgress> findByStudentId(UUID studentId);

    boolean existsByStudentIdAndLessonId(UUID studentId, UUID lessonId);
}
