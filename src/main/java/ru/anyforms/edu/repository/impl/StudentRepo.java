package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.user.Student;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface StudentRepo extends JpaRepository<Student, UUID> {

    Optional<Student> findByEmail(String email);

    List<Student> findAllByOrderByCreatedAtDesc();

    List<Student> findByEmailContainingIgnoreCaseOrderByCreatedAtDesc(String emailPart);
}
