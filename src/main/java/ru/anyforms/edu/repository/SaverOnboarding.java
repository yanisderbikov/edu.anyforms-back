package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.course.OnboardingSlide;

import java.util.List;

public interface SaverOnboarding {

    OnboardingSlide saveSlide(OnboardingSlide slide);

    List<OnboardingSlide> saveSlides(List<OnboardingSlide> slides);

    void deleteSlide(OnboardingSlide slide);
}
