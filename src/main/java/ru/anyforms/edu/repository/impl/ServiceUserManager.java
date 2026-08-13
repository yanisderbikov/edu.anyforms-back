package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.repository.GetterServiceUser;
import ru.anyforms.edu.repository.SaverServiceUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
class ServiceUserManager implements GetterServiceUser, SaverServiceUser {

    private final ServiceUserRepo serviceUserRepo;

    @Override
    public List<ServiceUser> getAll() {
        try {
            return serviceUserRepo.findAll();
        } catch (Exception e) {
            log.error("getAll failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

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
    public Optional<ServiceUser> getById(UUID id) {
        try {
            return serviceUserRepo.findById(id);
        } catch (Exception e) {
            log.error("getById failed", e);
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

    @Override
    public void delete(ServiceUser user) {
        try {
            serviceUserRepo.delete(user);
        } catch (Exception e) {
            log.error("delete failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
