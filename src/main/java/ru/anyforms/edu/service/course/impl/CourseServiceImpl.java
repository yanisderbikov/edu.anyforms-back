package ru.anyforms.edu.service.course.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.course.CourseResponseDTO;
import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.repository.ProgressStore;
import ru.anyforms.edu.service.activity.ActivityTracker;
import ru.anyforms.edu.service.course.CourseService;
import ru.anyforms.edu.service.s3.S3FileStorage;
import ru.anyforms.edu.util.MskTime;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
class CourseServiceImpl implements CourseService {

    private final GetterCourse getterCourse;
    private final GetterStudent getterStudent;
    private final ProgressStore progressStore;
    private final S3FileStorage s3FileStorage;
    private final ActivityTracker activityTracker;

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDTO getPublicCourse(String email) {
        return buildResponse(false, false, completedIds(email));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDTO.ModuleDTO getPublicModule(String email, boolean admin, UUID moduleId) {
        CourseModule module = getterCourse.getModuleById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Модуль не найден: " + moduleId));
        Optional<Student> student = getterStudent.getByEmail(email);
        // Заход студента на открытый модуль — след для аналитики «открыл, но не начал».
        // Админ не студент, закрытый модуль содержимого не показывает — их не считаем
        if (!admin && module.isOpen()) {
            student.ifPresent(s -> activityTracker.moduleVisited(s.getId(), module.getId()));
        }
        // Админу страница модуля открыта и до даты открытия — проверить её глазами
        // студента. Статус тоже приходит open, иначе фронт спрячет содержимое.
        // Список модулей на главной и админка статус не подменяют
        return toModuleDTO(module, false, true, admin, completedIds(student));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDTO getAdminCourse() {
        return buildResponse(true, true, Set.of());
    }

    /** Досмотренные уроки этого студента; у админа и незнакомого email — пусто. */
    private Set<UUID> completedIds(String email) {
        return completedIds(getterStudent.getByEmail(email));
    }

    private Set<UUID> completedIds(Optional<Student> student) {
        return student
                .map(s -> Set.copyOf(progressStore.getCompletedLessonIds(s.getId())))
                .orElseGet(Set::of);
    }

    private CourseResponseDTO buildResponse(boolean admin, boolean withLessons, Set<UUID> completed) {
        Course course = getterCourse.getBySlug(Course.DEFAULT_SLUG)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));

        List<CourseResponseDTO.ModuleDTO> modules = course.getModules().stream()
                .map(m -> toModuleDTO(m, admin, withLessons, false, completed))
                .toList();

        return new CourseResponseDTO(
                new CourseResponseDTO.CourseDTO(
                        course.getSlug(), course.getTitle(), course.getSubtitle(), modules.size()),
                new CourseResponseDTO.SupportDTO(
                        course.getChatLabel(), course.getChatUrl(),
                        course.getSupportLabel(), course.getSupportUrl()),
                modules
        );
    }

    /** adminPreview — админ смотрит страницу модуля до даты открытия: отдаём его как открытый */
    private CourseResponseDTO.ModuleDTO toModuleDTO(CourseModule module, boolean admin,
                                                    boolean withLessons, boolean adminPreview,
                                                    Set<UUID> completed) {
        boolean open = module.isOpen() || adminPreview;
        List<Lesson> moduleLessons = module.getLessons();

        // Уроки нужны только на странице модуля и в админке; у закрытого модуля
        // содержимое наружу не отдаём вовсе
        List<CourseResponseDTO.LessonDTO> lessons = withLessons && (open || admin)
                ? moduleLessons.stream().map(l -> toLessonDTO(l, admin)).toList()
                : List.of();

        int done = (int) moduleLessons.stream()
                .filter(l -> completed.contains(l.getId()))
                .count();

        return new CourseResponseDTO.ModuleDTO(
                module.getId().toString(),
                module.getOrd(),
                module.getTitle(),
                module.getDescription(),
                // Текст страницы модуля — как видео и уроки, у закрытого наружу не отдаём
                open || admin ? module.getVideoDescription() : null,
                s3FileStorage.resolveUrl(module.getImageUrl()),
                admin ? module.getImageUrl() : null,
                s3FileStorage.resolveUrl(module.getCoverUrl()),
                admin ? module.getCoverUrl() : null,
                // Видео закрытого модуля студенту не отдаём — как и уроки
                open || admin ? s3FileStorage.resolveUrl(module.getVideoUrl()) : null,
                admin ? module.getVideoUrl() : null,
                open || admin ? s3FileStorage.resolveUrl(module.getVideoCoverUrl()) : null,
                admin ? module.getVideoCoverUrl() : null,
                open ? "open" : "locked",
                MskTime.format(module.getOpensAt()),
                moduleLessons.size(),
                done,
                lessons
        );
    }

    private CourseResponseDTO.LessonDTO toLessonDTO(Lesson lesson, boolean admin) {
        List<CourseResponseDTO.LessonFileDTO> files = lesson.getFiles().stream()
                .map(f -> new CourseResponseDTO.LessonFileDTO(
                        f.getId().toString(),
                        f.getName(),
                        s3FileStorage.resolveDownloadUrl(f.getFileUrl(), f.getName()),
                        f.getSizeBytes()))
                .toList();

        return new CourseResponseDTO.LessonDTO(
                lesson.getId().toString(),
                lesson.getTitle(),
                lesson.getDescription(),
                s3FileStorage.resolveUrl(lesson.getVideoUrl()),
                admin ? lesson.getVideoUrl() : null,
                s3FileStorage.resolveUrl(lesson.getCoverUrl()),
                admin ? lesson.getCoverUrl() : null,
                files
        );
    }
}
