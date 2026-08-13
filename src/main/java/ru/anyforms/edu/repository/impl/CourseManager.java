package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.repository.SaverCourse;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
class CourseManager implements GetterCourse, SaverCourse {

    private final CourseRepo courseRepo;
    private final CourseModuleRepo moduleRepo;
    private final LessonRepo lessonRepo;

    @Override
    public Optional<Course> getBySlug(String slug) {
        try {
            return courseRepo.findBySlug(slug);
        } catch (Exception e) {
            log.error("getBySlug failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public Optional<CourseModule> getModuleById(UUID id) {
        try {
            return moduleRepo.findById(id);
        } catch (Exception e) {
            log.error("getModuleById failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public Optional<Lesson> getLessonById(UUID id) {
        try {
            return lessonRepo.findById(id);
        } catch (Exception e) {
            log.error("getLessonById failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public Course saveCourse(Course course) {
        try {
            return courseRepo.save(course);
        } catch (Exception e) {
            log.error("saveCourse failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public CourseModule saveModule(CourseModule module) {
        try {
            return moduleRepo.save(module);
        } catch (Exception e) {
            log.error("saveModule failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public Lesson saveLesson(Lesson lesson) {
        try {
            return lessonRepo.save(lesson);
        } catch (Exception e) {
            log.error("saveLesson failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public void deleteModule(CourseModule module) {
        try {
            moduleRepo.delete(module);
        } catch (Exception e) {
            log.error("deleteModule failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public void deleteLesson(Lesson lesson) {
        try {
            lessonRepo.delete(lesson);
        } catch (Exception e) {
            log.error("deleteLesson failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
