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
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.service.course.CourseService;
import ru.anyforms.edu.service.s3.S3FileStorage;

import java.util.List;

@Service
@AllArgsConstructor
class CourseServiceImpl implements CourseService {

    private final GetterCourse getterCourse;
    private final S3FileStorage s3FileStorage;

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDTO getPublicCourse() {
        return buildResponse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDTO getAdminCourse() {
        return buildResponse(true);
    }

    private CourseResponseDTO buildResponse(boolean admin) {
        Course course = getterCourse.getBySlug(Course.DEFAULT_SLUG)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));

        List<CourseResponseDTO.ModuleDTO> modules = course.getModules().stream()
                .map(m -> toModuleDTO(m, admin))
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

    private CourseResponseDTO.ModuleDTO toModuleDTO(CourseModule module, boolean admin) {
        boolean open = module.isOpen();
        // Видео закрытых модулей наружу не отдаём
        List<CourseResponseDTO.LessonDTO> lessons = (open || admin)
                ? module.getLessons().stream().map(l -> toLessonDTO(l, admin)).toList()
                : List.of();

        return new CourseResponseDTO.ModuleDTO(
                module.getId().toString(),
                module.getOrd(),
                module.getTitle(),
                module.getDescription(),
                s3FileStorage.resolveUrl(module.getImageUrl()),
                admin ? module.getImageUrl() : null,
                open ? "open" : "locked",
                module.getOpensAt() == null ? null : module.getOpensAt().toString(),
                lessons
        );
    }

    private CourseResponseDTO.LessonDTO toLessonDTO(Lesson lesson, boolean admin) {
        return new CourseResponseDTO.LessonDTO(
                lesson.getId().toString(),
                lesson.getTitle(),
                lesson.getDescription(),
                s3FileStorage.resolveUrl(lesson.getVideoUrl()),
                admin ? lesson.getVideoUrl() : null,
                s3FileStorage.resolveUrl(lesson.getCoverUrl()),
                admin ? lesson.getCoverUrl() : null
        );
    }
}
