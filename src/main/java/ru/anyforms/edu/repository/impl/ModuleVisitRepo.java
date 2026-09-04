package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.anyforms.edu.model.user.ModuleVisit;

import java.util.UUID;

@Repository
interface ModuleVisitRepo extends JpaRepository<ModuleVisit, Long> {

    /** Первый заход — новая строка, дальше — счётчик и время последнего захода. Одним запросом, без гонок */
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO module_visit (student_id, module_id, first_visited_at, last_visited_at, visits)
            VALUES (:studentId, :moduleId, now(), now(), 1)
            ON CONFLICT (student_id, module_id)
                DO UPDATE SET last_visited_at = now(), visits = module_visit.visits + 1
            """, nativeQuery = true)
    int upsertVisit(@Param("studentId") UUID studentId, @Param("moduleId") UUID moduleId);
}
