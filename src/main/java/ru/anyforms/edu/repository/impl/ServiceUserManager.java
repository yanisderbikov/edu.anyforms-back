package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.repository.GetterServiceUser;

import java.util.Optional;

/** Админы платформы заводятся вручную в базе, отсюда только чтение. */
@Component
@AllArgsConstructor
@Slf4j
class ServiceUserManager implements GetterServiceUser {

    private final ServiceUserRepo serviceUserRepo;

    @Override
    public Optional<ServiceUser> getByEmail(String email) {
        try {
            return serviceUserRepo.findByEmail(ServiceUser.normalizeEmail(email));
        } catch (Exception e) {
            log.error("getByEmail failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
