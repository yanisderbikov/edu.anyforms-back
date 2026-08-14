package ru.anyforms.edu.service.onboarding;

import ru.anyforms.edu.dto.admin.SlideRequestDTO;
import ru.anyforms.edu.dto.course.OnboardingResponseDTO;

import java.util.UUID;

public interface OnboardingService {

    OnboardingResponseDTO getOnboarding(boolean admin);

    UUID createSlide(SlideRequestDTO request);

    void updateSlide(UUID slideId, SlideRequestDTO request);

    void deleteSlide(UUID slideId);
}
