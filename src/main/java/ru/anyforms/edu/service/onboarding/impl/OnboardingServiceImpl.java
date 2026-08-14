package ru.anyforms.edu.service.onboarding.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.SlideRequestDTO;
import ru.anyforms.edu.dto.course.CourseResponseDTO;
import ru.anyforms.edu.dto.course.OnboardingResponseDTO;
import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.OnboardingSlide;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.repository.GetterOnboarding;
import ru.anyforms.edu.repository.SaverOnboarding;
import ru.anyforms.edu.service.onboarding.OnboardingService;
import ru.anyforms.edu.service.s3.S3FileStorage;
import ru.anyforms.edu.util.Ordering;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
class OnboardingServiceImpl implements OnboardingService {

    private final GetterCourse getterCourse;
    private final GetterOnboarding getterOnboarding;
    private final SaverOnboarding saverOnboarding;
    private final S3FileStorage s3FileStorage;

    private Course requireCourse() {
        return getterCourse.getBySlug(Course.DEFAULT_SLUG)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));
    }

    private OnboardingSlide requireSlide(UUID slideId) {
        return getterOnboarding.getSlideById(slideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Слайд не найден: " + slideId));
    }

    private static String joinPoints(List<String> points) {
        return points == null || points.isEmpty() ? null : String.join("\n", points);
    }

    private static String normalizeKind(String kind) {
        if (kind == null || kind.isBlank()) return OnboardingSlide.KIND_TEXT;
        return switch (kind.toUpperCase()) {
            case OnboardingSlide.KIND_SUPPORT -> OnboardingSlide.KIND_SUPPORT;
            case OnboardingSlide.KIND_FINAL -> OnboardingSlide.KIND_FINAL;
            default -> OnboardingSlide.KIND_TEXT;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public OnboardingResponseDTO getOnboarding(boolean admin) {
        Course course = requireCourse();
        List<OnboardingResponseDTO.SlideDTO> slides = getterOnboarding.getSlides(course.getId()).stream()
                .map(s -> toDTO(s, admin))
                .toList();
        return new OnboardingResponseDTO(slides, new CourseResponseDTO.SupportDTO(
                course.getChatLabel(), course.getChatUrl(),
                course.getSupportLabel(), course.getSupportUrl()));
    }

    private OnboardingResponseDTO.SlideDTO toDTO(OnboardingSlide slide, boolean admin) {
        return new OnboardingResponseDTO.SlideDTO(
                slide.getId().toString(),
                slide.getOrd(),
                slide.getKind(),
                slide.getEyebrow(),
                slide.getTitle(),
                slide.getBody(),
                slide.pointsList(),
                s3FileStorage.resolveUrl(slide.getImageUrl()),
                admin ? slide.getImageUrl() : null
        );
    }

    @Override
    @Transactional
    public UUID createSlide(SlideRequestDTO request) {
        Course course = requireCourse();
        OnboardingSlide slide = saverOnboarding.saveSlide(OnboardingSlide.builder()
                .course(course)
                .ord(request.getOrder())
                .kind(normalizeKind(request.getKind()))
                .eyebrow(request.getEyebrow())
                .title(request.getTitle())
                .body(request.getBody())
                .points(joinPoints(request.getPoints()))
                .imageUrl(request.getImageUrl())
                .build());
        resequence(course.getId(), slide.getId());
        return slide.getId();
    }

    @Override
    @Transactional
    public void updateSlide(UUID slideId, SlideRequestDTO request) {
        OnboardingSlide slide = requireSlide(slideId);
        slide.setOrd(request.getOrder());
        slide.setKind(normalizeKind(request.getKind()));
        slide.setEyebrow(request.getEyebrow());
        slide.setTitle(request.getTitle());
        slide.setBody(request.getBody());
        slide.setPoints(joinPoints(request.getPoints()));
        slide.setImageUrl(request.getImageUrl());
        saverOnboarding.saveSlide(slide);
        resequence(slide.getCourse().getId(), slideId);
    }

    @Override
    @Transactional
    public void deleteSlide(UUID slideId) {
        OnboardingSlide slide = requireSlide(slideId);
        UUID courseId = slide.getCourse().getId();
        saverOnboarding.deleteSlide(slide);
        resequence(courseId, null);
    }

    /** После сохранения слайды выстраиваются подряд: 1, 2, 3… */
    private void resequence(UUID courseId, UUID justSavedId) {
        List<OnboardingSlide> ordered = Ordering.reorder(
                getterOnboarding.getSlides(courseId),
                OnboardingSlide::getOrd, OnboardingSlide::getId, justSavedId,
                OnboardingSlide::setOrd);
        saverOnboarding.saveSlides(ordered);
    }
}
