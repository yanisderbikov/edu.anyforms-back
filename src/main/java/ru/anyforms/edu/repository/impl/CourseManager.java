package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;
import ru.anyforms.edu.model.course.LessonFile;
import ru.anyforms.edu.model.course.ModuleFile;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.repository.SaverCourse;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
class CourseManager implements GetterCourse, SaverCourse {

    private final CourseRepo courseRepo;
    private final CourseModuleRepo moduleRepo;
    private final LessonRepo lessonRepo;
    private final LessonFileRepo fileRepo;
    private final ModuleFileRepo moduleFileRepo;

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
    public Optional<LessonFile> getFileById(UUID id) {
        try {
            return fileRepo.findById(id);
        } catch (Exception e) {
            log.error("getFileById failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public Optional<ModuleFile> getModuleFileById(UUID id) {
        try {
            return moduleFileRepo.findById(id);
        } catch (Exception e) {
            log.error("getModuleFileById failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public List<CourseModule> getModules(UUID courseId) {
        try {
            return moduleRepo.findByCourseIdOrderByOrdAsc(courseId);
        } catch (Exception e) {
            log.error("getModules failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public List<Lesson> getLessons(UUID moduleId) {
        try {
            return lessonRepo.findByModuleIdOrderByOrdAsc(moduleId);
        } catch (Exception e) {
            log.error("getLessons failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public List<CourseModule> getModulesToAnnounceOpen(Instant now) {
        try {
            return moduleRepo.findToAnnounceOpen(now);
        } catch (Exception e) {
            log.error("getModulesToAnnounceOpen failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public boolean isAssetInUse(String urlOrKey) {
        try {
            return lessonRepo.countByVideoUrlOrCoverUrl(urlOrKey, urlOrKey) > 0
                    || fileRepo.countAliveByFileUrl(urlOrKey) > 0
                    || moduleRepo.countByAnyAsset(urlOrKey) > 0
                    || moduleFileRepo.countByFileUrl(urlOrKey) > 0;
        } catch (Exception e) {
            log.error("isAssetInUse failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public List<CourseModule> saveModules(List<CourseModule> modules) {
        try {
            return moduleRepo.saveAll(modules);
        } catch (Exception e) {
            log.error("saveModules failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public List<Lesson> saveLessons(List<Lesson> lessons) {
        try {
            return lessonRepo.saveAll(lessons);
        } catch (Exception e) {
            log.error("saveLessons failed", e);
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
    public LessonFile saveFile(LessonFile file) {
        try {
            return fileRepo.save(file);
        } catch (Exception e) {
            log.error("saveFile failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public void deleteFile(LessonFile file) {
        try {
            fileRepo.delete(file);
        } catch (Exception e) {
            log.error("deleteFile failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public ModuleFile saveModuleFile(ModuleFile file) {
        try {
            return moduleFileRepo.save(file);
        } catch (Exception e) {
            log.error("saveModuleFile failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public void deleteModuleFile(ModuleFile file) {
        try {
            moduleFileRepo.delete(file);
        } catch (Exception e) {
            log.error("deleteModuleFile failed", e);
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
            // Мягко: строку не трогаем, иначе каскадом уедет прогресс студентов.
            // Из выборок урок пропадёт сам — фильтр стоит на сущности (@SQLRestriction)
            lesson.setDeletedAt(Instant.now());
            lessonRepo.save(lesson);
        } catch (Exception e) {
            log.error("deleteLesson failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
