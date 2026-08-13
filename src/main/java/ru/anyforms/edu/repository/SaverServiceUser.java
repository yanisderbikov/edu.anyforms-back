package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.user.ServiceUser;

public interface SaverServiceUser {

    ServiceUser save(ServiceUser user);

    void delete(ServiceUser user);
}
