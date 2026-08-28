package ru.anyforms.edu.service.admin;

import ru.anyforms.edu.dto.admin.CourseRequestDTO;
import ru.anyforms.edu.dto.admin.LessonFileRequestDTO;
import ru.anyforms.edu.dto.admin.LessonRequestDTO;
import ru.anyforms.edu.dto.admin.ModuleRequestDTO;

import java.util.UUID;

public interface AdminCourseService {

    void updateCourse(CourseRequestDTO request);

    UUID createModule(ModuleRequestDTO request);

    void updateModule(UUID moduleId, ModuleRequestDTO request);

    void deleteModule(UUID moduleId);

    UUID createLesson(UUID moduleId, LessonRequestDTO request);

    void updateLesson(UUID lessonId, LessonRequestDTO request);

    void deleteLesson(UUID lessonId);

    UUID addLessonFile(UUID lessonId, LessonFileRequestDTO request);

    void deleteLessonFile(UUID fileId);
}
