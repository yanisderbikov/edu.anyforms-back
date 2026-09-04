package ru.anyforms.edu.model.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Заходы клиента на страницу открытого модуля: одна строка на пару студент+модуль.
 * Отличает «открыл модуль, но ничего не запускал» от «не открывал вовсе».
 * Пишется upsert'ом в фоне (ActivityTracker), сущность нужна только для чтения в аналитике.
 */
@Entity
@Table(name = "module_visit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "module_id", nullable = false)
    private UUID moduleId;

    @Column(name = "first_visited_at", nullable = false)
    private Instant firstVisitedAt;

    @Column(name = "last_visited_at", nullable = false)
    private Instant lastVisitedAt;

    /** Сколько раз страница модуля загружалась с сервера (переходы внутри вкладки идут из кэша фронта) */
    @Column(nullable = false)
    private Integer visits;
}
