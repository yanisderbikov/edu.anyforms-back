package ru.anyforms.edu.service.admin.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.CourseRequestDTO;
import ru.anyforms.edu.dto.admin.LessonRequestDTO;
import ru.anyforms.edu.dto.admin.ModuleRequestDTO;
import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.repository.SaverCourse;
import ru.anyforms.edu.service.admin.AdminCourseService;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
class AdminCourseServiceImpl implements AdminCourseService {

    private final GetterCourse getterCourse;
    private final SaverCourse saverCourse;

    private Course requireCourse() {
        return getterCourse.getBySlug(Course.DEFAULT_SLUG)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));
    }

    private CourseModule requireModule(UUID moduleId) {
        return getterCourse.getModuleById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Модуль не найден: " + moduleId));
    }

    private Lesson requireLesson(UUID lessonId) {
        return getterCourse.getLessonById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Урок не найден: " + lessonId));
    }

    private static String joinPoints(List<String> points) {
        return points == null || points.isEmpty() ? null : String.join("\n", points);
    }

    @Override
    @Transactional
    public void updateCourse(CourseRequestDTO request) {
        Course course = requireCourse();
        course.setTitle(request.getTitle());
        course.setSubtitle(request.getSubtitle());
        if (request.getChatLabel() != null) course.setChatLabel(request.getChatLabel());
        course.setChatUrl(request.getChatUrl());
        if (request.getSupportLabel() != null) course.setSupportLabel(request.getSupportLabel());
        course.setSupportUrl(request.getSupportUrl());
        saverCourse.saveCourse(course);
    }

    @Override
    @Transactional
    public UUID createModule(ModuleRequestDTO request) {
        CourseModule module = CourseModule.builder()
                .course(requireCourse())
                .ord(request.getOrder())
                .title(request.getTitle())
                .description(request.getDescription())
                .points(joinPoints(request.getPoints()))
                .imageUrl(request.getImageUrl())
                .opensAt(request.getOpensAt())
                .build();
        return saverCourse.saveModule(module).getId();
    }

    @Override
    @Transactional
    public void updateModule(UUID moduleId, ModuleRequestDTO request) {
        CourseModule module = requireModule(moduleId);
        module.setOrd(request.getOrder());
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setPoints(joinPoints(request.getPoints()));
        module.setImageUrl(request.getImageUrl());
        module.setOpensAt(request.getOpensAt());
        saverCourse.saveModule(module);
    }

    @Override
    @Transactional
    public void deleteModule(UUID moduleId) {
        saverCourse.deleteModule(requireModule(moduleId));
    }

    @Override
    @Transactional
    public UUID createLesson(UUID moduleId, LessonRequestDTO request) {
        Lesson lesson = Lesson.builder()
                .module(requireModule(moduleId))
                .ord(request.getOrder())
                .title(request.getTitle())
                .description(request.getDescription())
                .videoUrl(request.getVideoUrl())
                .build();
        return saverCourse.saveLesson(lesson).getId();
    }

    @Override
    @Transactional
    public void updateLesson(UUID lessonId, LessonRequestDTO request) {
        Lesson lesson = requireLesson(lessonId);
        lesson.setOrd(request.getOrder());
        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setVideoUrl(request.getVideoUrl());
        saverCourse.saveLesson(lesson);
    }

    @Override
    @Transactional
    public void deleteLesson(UUID lessonId) {
        saverCourse.deleteLesson(requireLesson(lessonId));
    }
}
