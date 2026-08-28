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
import ru.anyforms.edu.service.cleanup.LessonAssetCleaner;
import ru.anyforms.edu.util.Ordering;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
class AdminCourseServiceImpl implements AdminCourseService {

    private final GetterCourse getterCourse;
    private final SaverCourse saverCourse;
    private final LessonAssetCleaner assetCleaner;

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
        CourseModule module = CourseModule.builder()
                .course(course)
                .ord(request.getOrder())
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .coverUrl(request.getCoverUrl())
                .videoUrl(request.getVideoUrl())
                .videoCoverUrl(request.getVideoCoverUrl())
                .opensAt(request.getOpensAt())
                .build();
        // Модуль, созданный сразу открытым, не «открывается» — письма об открытии
        // не шлём. Они уйдут, только если задана будущая дата и она наступит
        if (module.isOpen()) {
            module.setOpenEmailQueuedAt(Instant.now());
        }
        module = saverCourse.saveModule(module);
        resequenceModules(course.getId(), module.getId());
        return module.getId();
    }

    @Override
    @Transactional
    public void updateModule(UUID moduleId, ModuleRequestDTO request) {
        CourseModule module = requireModule(moduleId);
        String replacedImage = replaced(module.getImageUrl(), request.getImageUrl());
        String replacedCover = replaced(module.getCoverUrl(), request.getCoverUrl());
        String replacedVideo = replaced(module.getVideoUrl(), request.getVideoUrl());
        String replacedVideoCover = replaced(module.getVideoCoverUrl(), request.getVideoCoverUrl());
        module.setOrd(request.getOrder());
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setImageUrl(request.getImageUrl());
        module.setCoverUrl(request.getCoverUrl());
        module.setVideoUrl(request.getVideoUrl());
        module.setVideoCoverUrl(request.getVideoCoverUrl());
        module.setOpensAt(request.getOpensAt());
        // Дату открытия перенесли в будущее — модуль снова закрыт: когда дата
        // наступит, объявим об открытии заново. Открытие руками (дата очищена или
        // в прошлом) подхватит планировщик, если про модуль ещё не объявляли
        if (!module.isOpen()) {
            module.setOpenEmailQueuedAt(null);
        }
        saverCourse.saveModule(module);
        resequenceModules(module.getCourse().getId(), moduleId);
        if (replacedImage != null) {
            assetCleaner.deleteAfterCommit(LessonAssetCleaner.Assets.ofCover(replacedImage));
        }
        if (replacedCover != null) {
            assetCleaner.deleteAfterCommit(LessonAssetCleaner.Assets.ofCover(replacedCover));
        }
        if (replacedVideo != null) {
            assetCleaner.deleteAfterCommit(LessonAssetCleaner.Assets.ofVideo(replacedVideo));
        }
        if (replacedVideoCover != null) {
            assetCleaner.deleteAfterCommit(LessonAssetCleaner.Assets.ofCover(replacedVideoCover));
        }
    }

    @Override
    @Transactional
    public void deleteModule(UUID moduleId) {
        CourseModule module = requireModule(moduleId);
        UUID courseId = module.getCourse().getId();
        List<LessonAssetCleaner.Assets> assets = new java.util.ArrayList<>(
                getterCourse.getLessons(moduleId).stream()
                        .map(LessonAssetCleaner.Assets::of)
                        .toList());
        assets.add(LessonAssetCleaner.Assets.ofCover(module.getImageUrl()));
        assets.add(LessonAssetCleaner.Assets.ofCover(module.getCoverUrl()));
        assets.add(LessonAssetCleaner.Assets.ofVideo(module.getVideoUrl()));
        assets.add(LessonAssetCleaner.Assets.ofCover(module.getVideoCoverUrl()));
        saverCourse.deleteModule(module);
        resequenceModules(courseId, null);
        assets.forEach(assetCleaner::deleteAfterCommit);
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
        String replacedVideo = replaced(lesson.getVideoUrl(), request.getVideoUrl());
        String replacedCover = replaced(lesson.getCoverUrl(), request.getCoverUrl());
        lesson.setOrd(request.getOrder());
        lesson.setTitle(request.getTitle() == null ? "" : request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setCoverUrl(request.getCoverUrl());
        saverCourse.saveLesson(lesson);
        resequenceLessons(lesson.getModule().getId(), lessonId);
        if (replacedVideo != null) {
            assetCleaner.deleteAfterCommit(LessonAssetCleaner.Assets.ofVideo(replacedVideo));
        }
        if (replacedCover != null) {
            assetCleaner.deleteAfterCommit(LessonAssetCleaner.Assets.ofCover(replacedCover));
        }
    }

    @Override
    @Transactional
    public void deleteLesson(UUID lessonId) {
        Lesson lesson = requireLesson(lessonId);
        UUID moduleId = lesson.getModule().getId();
        LessonAssetCleaner.Assets assets = LessonAssetCleaner.Assets.of(lesson);
        saverCourse.deleteLesson(lesson);
        resequenceLessons(moduleId, null);
        assetCleaner.deleteAfterCommit(assets);
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
        String fileUrl = file.getFileUrl();
        saverCourse.deleteFile(file);
        assetCleaner.deleteAfterCommit(LessonAssetCleaner.Assets.ofFile(fileUrl));
    }

    /**
     * Старое значение поля, если оно действительно уходит из урока
     * (заменили другим или очистили); null — менять нечего.
     */
    private String replaced(String was, String now) {
        if (was == null || was.isBlank() || was.equals(now)) {
            return null;
        }
        return was;
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
