package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.course.OnboardingSlide;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetterOnboarding {

    List<OnboardingSlide> getSlides(UUID courseId);

    Optional<OnboardingSlide> getSlideById(UUID id);
}
