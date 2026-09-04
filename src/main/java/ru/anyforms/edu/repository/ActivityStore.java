package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.user.ModuleVisit;

import java.util.List;
import java.util.UUID;

/** Заходы студентов на страницы модулей — для аналитики «открыл, но не начал». */
public interface ActivityStore {

    /** Первый раз — новая строка, дальше — счётчик и время последнего захода */
    void recordModuleVisit(UUID studentId, UUID moduleId);

    /** Все заходы всех студентов — для аналитики */
    List<ModuleVisit> getAllVisits();
}
