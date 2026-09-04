package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.user.Student;

public interface SaverStudent {

    Student save(Student student);

    void delete(Student student);

    /**
     * Отмечает, что клиент сейчас на платформе: last_seen_at = сейчас,
     * first_seen_at — при первой отметке. В БД пишет не чаще раза в несколько
     * минут (по last_seen_at переданной сущности), чтобы не делать UPDATE
     * на каждый запрос. Сущность не меняет.
     */
    void touchSeen(Student student);
}
