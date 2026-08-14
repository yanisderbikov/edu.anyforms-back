package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetterCourse {

    Optional<Course> getBySlug(String slug);

    Optional<CourseModule> getModuleById(UUID id);

    Optional<Lesson> getLessonById(UUID id);

    /** Для перенумерации: модули курса по возрастанию порядка */
    List<CourseModule> getModules(UUID courseId);

    /** Для перенумерации: уроки модуля по возрастанию порядка */
    List<Lesson> getLessons(UUID moduleId);
}
