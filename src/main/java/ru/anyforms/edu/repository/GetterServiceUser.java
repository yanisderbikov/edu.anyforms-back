package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.user.ServiceUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetterServiceUser {

    List<ServiceUser> getAll();

    Optional<ServiceUser> getByEmail(String email);

    Optional<ServiceUser> getById(UUID id);
}
