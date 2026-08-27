package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.user.ServiceUser;

import java.util.List;
import java.util.Optional;

/** Админы платформы (таблица service_user): назначаются из админки аккаунтов. */
public interface GetterServiceUser {

    Optional<ServiceUser> getByEmail(String email);

    List<ServiceUser> getActive();
}
