package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.course.LessonFile;

import java.util.UUID;

@Repository
interface LessonFileRepo extends JpaRepository<LessonFile, UUID> {
}
