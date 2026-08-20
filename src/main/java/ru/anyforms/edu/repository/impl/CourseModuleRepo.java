package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.course.CourseModule;

import java.util.List;
import java.util.UUID;

@Repository
interface CourseModuleRepo extends JpaRepository<CourseModule, UUID> {

    List<CourseModule> findByCourseIdOrderByOrdAsc(UUID courseId);

    long countByImageUrl(String imageUrl);
}
