package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.user.ModuleVisit;
import ru.anyforms.edu.repository.ActivityStore;

import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
class ActivityManager implements ActivityStore {

    private final ModuleVisitRepo moduleVisitRepo;

    @Override
    public void recordModuleVisit(UUID studentId, UUID moduleId) {
        try {
            moduleVisitRepo.upsertVisit(studentId, moduleId);
        } catch (Exception e) {
            log.error("recordModuleVisit failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public List<ModuleVisit> getAllVisits() {
        try {
            return moduleVisitRepo.findAll();
        } catch (Exception e) {
            log.error("getAllVisits failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
