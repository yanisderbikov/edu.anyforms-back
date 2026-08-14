package ru.anyforms.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.anyforms.edu.dto.course.OnboardingResponseDTO;
import ru.anyforms.edu.service.onboarding.OnboardingService;

/** Онбординг для платформы — только для залогиненных. */
@AllArgsConstructor
@RestController
@RequestMapping("/api/onboarding")
@Tag(name = "Onboarding", description = "Слайды онбординга (требуется JWT)")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "Слайды онбординга",
            description = "Слайды по порядку + ссылки поддержки для слайда с kind = SUPPORT",
            security = @SecurityRequirement(name = "Bearer"))
    @GetMapping
    public ResponseEntity<OnboardingResponseDTO> getOnboarding() {
        return ResponseEntity.ok(onboardingService.getOnboarding(false));
    }
}
