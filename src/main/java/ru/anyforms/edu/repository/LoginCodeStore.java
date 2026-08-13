package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.user.LoginCode;

import java.util.Optional;

public interface LoginCodeStore {

    Optional<LoginCode> getLatestActive(String email);

    LoginCode save(LoginCode code);

    void invalidateAll(String email);
}
