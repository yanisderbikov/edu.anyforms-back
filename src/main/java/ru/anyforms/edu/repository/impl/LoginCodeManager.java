package ru.anyforms.edu.repository.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.user.LoginCode;
import ru.anyforms.edu.repository.LoginCodeStore;

import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
class LoginCodeManager implements LoginCodeStore {

    private final LoginCodeRepo loginCodeRepo;

    @Override
    public Optional<LoginCode> getLatestActive(String email) {
        try {
            return loginCodeRepo.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email);
        } catch (Exception e) {
            log.error("getLatestActive failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public LoginCode save(LoginCode code) {
        try {
            return loginCodeRepo.save(code);
        } catch (Exception e) {
            log.error("save failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    @Transactional
    public void invalidateAll(String email) {
        try {
            loginCodeRepo.invalidateAllByEmail(email);
        } catch (Exception e) {
            log.error("invalidateAll failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
