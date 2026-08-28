package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.repository.GetterServiceUser;
import ru.anyforms.edu.repository.SaverServiceUser;

import java.util.List;
import java.util.Optional;

/** Админы платформы: читаются при входе, назначаются из админки аккаунтов. */
@Component
@AllArgsConstructor
@Slf4j
class ServiceUserManager implements GetterServiceUser, SaverServiceUser {

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

    @Override
    public List<ServiceUser> getActive() {
        try {
            return serviceUserRepo.findByActiveTrue();
        } catch (Exception e) {
            log.error("getActive failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public ServiceUser save(ServiceUser user) {
        try {
            return serviceUserRepo.save(user);
        } catch (Exception e) {
            log.error("save failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
