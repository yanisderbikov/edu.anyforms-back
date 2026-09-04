package ru.anyforms.edu.service.analytics;

import ru.anyforms.edu.dto.admin.AnalyticsDTO;

public interface AnalyticsService {

    /** Прогресс всех клиентов по модулям курса. Админы (service_user) не входят */
    AnalyticsDTO getStudents();
}
