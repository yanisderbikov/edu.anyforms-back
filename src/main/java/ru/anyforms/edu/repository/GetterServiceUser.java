package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.user.ServiceUser;

import java.util.Optional;

/** Админы платформы: список ведётся вручную в таблице service_user. */
public interface GetterServiceUser {

    Optional<ServiceUser> getByEmail(String email);
}
