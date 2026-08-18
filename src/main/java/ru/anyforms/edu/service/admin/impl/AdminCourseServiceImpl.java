package ru.anyforms.edu.service.admin.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.CourseRequestDTO;
import ru.anyforms.edu.dto.admin.LessonFileRequestDTO;
import ru.anyforms.edu.dto.admin.LessonRequestDTO;
import ru.anyforms.edu.dto.admin.ModuleRequestDTO;
import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;
import ru.anyforms.edu.model.course.LessonFile;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.repository.SaverCourse;
import ru.anyforms.edu.service.admin.AdminCourseService;
import ru.anyforms.edu.util.Ordering;

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
        Course course = requireCourse();
        CourseModule module = saverCourse.saveModule(CourseModule.builder()
                .course(course)
                .ord(request.getOrder())
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .opensAt(request.getOpensAt())
                .build());
        resequenceModules(course.getId(), module.getId());
        return module.getId();
    }

    @Override
    @Transactional
    public void updateModule(UUID moduleId, ModuleRequestDTO request) {
        CourseModule module = requireModule(moduleId);
        module.setOrd(request.getOrder());
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setImageUrl(request.getImageUrl());
        module.setOpensAt(request.getOpensAt());
        saverCourse.saveModule(module);
        resequenceModules(module.getCourse().getId(), moduleId);
    }

    @Override
    @Transactional
    public void deleteModule(UUID moduleId) {
        CourseModule module = requireModule(moduleId);
        UUID courseId = module.getCourse().getId();
        saverCourse.deleteModule(module);
        resequenceModules(courseId, null);
    }

    @Override
    @Transactional
    public UUID createLesson(UUID moduleId, LessonRequestDTO request) {
        Lesson lesson = saverCourse.saveLesson(Lesson.builder()
                .module(requireModule(moduleId))
                .ord(request.getOrder())
                .title(request.getTitle() == null ? "" : request.getTitle())
                .description(request.getDescription())
                .videoUrl(request.getVideoUrl())
                .coverUrl(request.getCoverUrl())
                .build());
        resequenceLessons(moduleId, lesson.getId());
        return lesson.getId();
    }

    @Override
    @Transactional
    public void updateLesson(UUID lessonId, LessonRequestDTO request) {
        Lesson lesson = requireLesson(lessonId);
        lesson.setOrd(request.getOrder());
        lesson.setTitle(request.getTitle() == null ? "" : request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setCoverUrl(request.getCoverUrl());
        saverCourse.saveLesson(lesson);
        resequenceLessons(lesson.getModule().getId(), lessonId);
    }

    @Override
    @Transactional
    public void deleteLesson(UUID lessonId) {
        Lesson lesson = requireLesson(lessonId);
        UUID moduleId = lesson.getModule().getId();
        saverCourse.deleteLesson(lesson);
        resequenceLessons(moduleId, null);
    }

    @Override
    @Transactional
    public UUID addLessonFile(UUID lessonId, LessonFileRequestDTO request) {
        LessonFile file = saverCourse.saveFile(LessonFile.builder()
                .lesson(requireLesson(lessonId))
                .name(request.getName())
                .fileUrl(request.getFileUrl())
                .sizeBytes(request.getSizeBytes())
                .build());
        return file.getId();
    }

    @Override
    @Transactional
    public void deleteLessonFile(UUID fileId) {
        LessonFile file = getterCourse.getFileById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден: " + fileId));
        saverCourse.deleteFile(file);
    }

    /** Модули выстраиваются подряд: 1, 2, 3… */
    private void resequenceModules(UUID courseId, UUID justSavedId) {
        saverCourse.saveModules(Ordering.reorder(
                getterCourse.getModules(courseId),
                CourseModule::getOrd, CourseModule::getId, justSavedId,
                CourseModule::setOrd));
    }

    /** Уроки внутри модуля выстраиваются подряд: 1, 2, 3… */
    private void resequenceLessons(UUID moduleId, UUID justSavedId) {
        saverCourse.saveLessons(Ordering.reorder(
                getterCourse.getLessons(moduleId),
                Lesson::getOrd, Lesson::getId, justSavedId,
                Lesson::setOrd));
    }
}
