package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.course.CourseModule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
interface CourseModuleRepo extends JpaRepository<CourseModule, UUID> {

    List<CourseModule> findByCourseIdOrderByOrdAsc(UUID courseId);

    /** Открывшиеся модули активных курсов, про которые письма ещё не ставились в очередь */
    @Query("""
            select m from CourseModule m
            where m.openEmailQueuedAt is null
              and (m.opensAt is null or m.opensAt <= :today)
              and m.course.active = true
            """)
    List<CourseModule> findToAnnounceOpen(@Param("today") LocalDate today);

    /** Сколько модулей ссылаются на файл любым из медиа-полей */
    @Query("""
            select count(m) from CourseModule m
            where m.imageUrl = :asset or m.coverUrl = :asset
               or m.videoUrl = :asset or m.videoCoverUrl = :asset
            """)
    long countByAnyAsset(@Param("asset") String asset);
}
