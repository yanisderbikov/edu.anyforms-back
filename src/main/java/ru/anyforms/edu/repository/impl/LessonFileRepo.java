package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.course.LessonFile;

import java.util.UUID;

@Repository
interface LessonFileRepo extends JpaRepository<LessonFile, UUID> {

    /**
     * Материалы живых уроков с этим ключом. Условие по deleted_at обязательно:
     * у мягко удалённого урока строки файлов остаются, и без него уборщик решил бы,
     * что файл ещё кому-то нужен, и не стал бы его удалять.
     */
    @Query("SELECT COUNT(f) FROM LessonFile f WHERE f.fileUrl = :fileUrl AND f.lesson.deletedAt IS NULL")
    long countAliveByFileUrl(@Param("fileUrl") String fileUrl);
}
