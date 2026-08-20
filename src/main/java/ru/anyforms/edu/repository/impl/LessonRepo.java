package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.course.Lesson;

import java.util.List;
import java.util.UUID;

@Repository
interface LessonRepo extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByModuleIdOrderByOrdAsc(UUID moduleId);

    /** Живые уроки (удалённые отсекает @SQLRestriction), где файл ещё используется */
    long countByVideoUrlOrCoverUrl(String videoUrl, String coverUrl);
}
