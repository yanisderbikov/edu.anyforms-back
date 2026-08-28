package ru.anyforms.edu.service.course;

import ru.anyforms.edu.dto.course.CourseResponseDTO;

import java.util.UUID;

public interface CourseService {

    /** Курс и превью модулей без уроков — для главной. */
    CourseResponseDTO getPublicCourse(String email);

    /** Один модуль с уроками — для страницы модуля. Уроки закрытого модуля не отдаём. */
    CourseResponseDTO.ModuleDTO getPublicModule(String email, UUID moduleId);

    /** Полный JSON для админки: все модули с уроками независимо от дат. */
    CourseResponseDTO getAdminCourse();
}
