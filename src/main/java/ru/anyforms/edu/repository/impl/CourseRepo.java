package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.course.Course;

import java.util.Optional;
import java.util.UUID;

@Repository
interface CourseRepo extends JpaRepository<Course, UUID> {

    Optional<Course> findBySlug(String slug);
}
