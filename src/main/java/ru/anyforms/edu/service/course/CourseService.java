package ru.anyforms.edu.service.course;

import ru.anyforms.edu.dto.course.CourseResponseDTO;

import java.util.UUID;

public interface CourseService {

    /** Курс и превью модулей без уроков — для главной. */
    CourseResponseDTO getPublicCourse(String email);

    /** Один модуль с уроками — для страницы модуля. Закрытый модуль студенту не отдаём, админу — как открытый. */
    CourseResponseDTO.ModuleDTO getPublicModule(String email, boolean admin, UUID moduleId);

    /** Полный JSON для админки: все модули с уроками независимо от дат. */
    CourseResponseDTO getAdminCourse();
}
