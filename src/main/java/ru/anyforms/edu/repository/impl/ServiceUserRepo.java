package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.user.ServiceUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface ServiceUserRepo extends JpaRepository<ServiceUser, UUID> {

    Optional<ServiceUser> findByEmail(String email);

    List<ServiceUser> findByActiveTrue();
}
