package ru.anyforms.edu.service.user.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.ServiceUserRequestDTO;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.repository.GetterServiceUser;
import ru.anyforms.edu.repository.SaverServiceUser;
import ru.anyforms.edu.service.user.ServiceUserService;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
class ServiceUserServiceImpl implements ServiceUserService {

    private final GetterServiceUser getterServiceUser;
    private final SaverServiceUser saverServiceUser;

    @Override
    public List<ServiceUser> getAll() {
        return getterServiceUser.getAll();
    }

    @Override
    public ServiceUser create(ServiceUserRequestDTO request) {
        String email = ServiceUser.normalizeEmail(request.getEmail());
        if (getterServiceUser.getByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь уже существует: " + email);
        }
        ServiceUser user = ServiceUser.builder()
                .email(email)
                .role(request.getRole() == null || request.getRole().isBlank() ? "ADMIN" : request.getRole())
                .build();
        return saverServiceUser.save(user);
    }

    @Override
    public void delete(UUID id) {
        ServiceUser user = getterServiceUser.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден: " + id));
        saverServiceUser.delete(user);
    }
}
