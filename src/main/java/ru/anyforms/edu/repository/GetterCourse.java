package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;
import ru.anyforms.edu.model.course.LessonFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetterCourse {

    Optional<Course> getBySlug(String slug);

    Optional<CourseModule> getModuleById(UUID id);

    Optional<Lesson> getLessonById(UUID id);

    Optional<LessonFile> getFileById(UUID id);

    /** Для перенумерации: модули курса по возрастанию порядка */
    List<CourseModule> getModules(UUID courseId);

    /** Для перенумерации: уроки модуля по возрастанию порядка */
    List<Lesson> getLessons(UUID moduleId);

    /** Модули активных курсов, которые уже открылись, но об открытии ещё не объявляли */
    List<CourseModule> getModulesToAnnounceOpen(LocalDate today);

    /**
     * Ссылается ли на этот файл (ключ S3 или ссылка Kinescope) кто-то ещё:
     * живой урок видео/обложкой либо запись материала. Удалённые уроки не считаются.
     */
    boolean isAssetInUse(String urlOrKey);
}
