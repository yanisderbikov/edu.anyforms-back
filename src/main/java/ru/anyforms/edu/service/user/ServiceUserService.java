package ru.anyforms.edu.service.user;

import ru.anyforms.edu.dto.admin.ServiceUserRequestDTO;
import ru.anyforms.edu.model.user.ServiceUser;

import java.util.List;
import java.util.UUID;

public interface ServiceUserService {

    List<ServiceUser> getAll();

    ServiceUser create(ServiceUserRequestDTO request);

    void delete(UUID id);
}
