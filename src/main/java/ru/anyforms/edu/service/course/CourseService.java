package ru.anyforms.edu.service.course;

import ru.anyforms.edu.dto.course.CourseResponseDTO;

public interface CourseService {

    /** Публичный JSON курса для фронтенда (уроки только у открытых модулей). */
    CourseResponseDTO getPublicCourse();

    /** Полный JSON для админки: все модули с уроками независимо от дат. */
    CourseResponseDTO getAdminCourse();
}
