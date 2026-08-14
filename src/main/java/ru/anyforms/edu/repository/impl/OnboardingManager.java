package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.course.OnboardingSlide;
import ru.anyforms.edu.repository.GetterOnboarding;
import ru.anyforms.edu.repository.SaverOnboarding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
class OnboardingManager implements GetterOnboarding, SaverOnboarding {

    private final OnboardingSlideRepo slideRepo;

    @Override
    public List<OnboardingSlide> getSlides(UUID courseId) {
        try {
            return slideRepo.findByCourseIdOrderByOrdAsc(courseId);
        } catch (Exception e) {
            log.error("getSlides failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public Optional<OnboardingSlide> getSlideById(UUID id) {
        try {
            return slideRepo.findById(id);
        } catch (Exception e) {
            log.error("getSlideById failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public OnboardingSlide saveSlide(OnboardingSlide slide) {
        try {
            return slideRepo.save(slide);
        } catch (Exception e) {
            log.error("saveSlide failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public List<OnboardingSlide> saveSlides(List<OnboardingSlide> slides) {
        try {
            return slideRepo.saveAll(slides);
        } catch (Exception e) {
            log.error("saveSlides failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public void deleteSlide(OnboardingSlide slide) {
        try {
            slideRepo.delete(slide);
        } catch (Exception e) {
            log.error("deleteSlide failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
