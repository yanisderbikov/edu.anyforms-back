package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;
import ru.anyforms.edu.model.course.LessonFile;
import ru.anyforms.edu.model.course.ModuleFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetterCourse {

    Optional<Course> getBySlug(String slug);

    Optional<CourseModule> getModuleById(UUID id);

    Optional<Lesson> getLessonById(UUID id);

    Optional<LessonFile> getFileById(UUID id);

    Optional<ModuleFile> getModuleFileById(UUID id);

    /** Для перенумерации: модули курса по возрастанию порядка */
    List<CourseModule> getModules(UUID courseId);

    /** Для перенумерации: уроки модуля по возрастанию порядка */
    List<Lesson> getLessons(UUID moduleId);

    /** Модули активных курсов, которые уже открылись, но об открытии ещё не объявляли */
    List<CourseModule> getModulesToAnnounceOpen(Instant now);

    /**
     * Ссылается ли на этот файл (ключ S3 или ссылка Kinescope) кто-то ещё:
     * живой урок видео/обложкой, модуль медиа-полями либо запись материала
     * урока или модуля. Удалённые уроки не считаются.
     */
    boolean isAssetInUse(String urlOrKey);
}
