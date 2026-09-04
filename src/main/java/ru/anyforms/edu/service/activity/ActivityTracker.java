package ru.anyforms.edu.service.activity;

import java.util.UUID;

/** Следы активности студента для аналитики. Пишутся в фоне: запрос студента БД не ждёт. */
public interface ActivityTracker {

    /** Студент открыл страницу открытого модуля */
    void moduleVisited(UUID studentId, UUID moduleId);
}
