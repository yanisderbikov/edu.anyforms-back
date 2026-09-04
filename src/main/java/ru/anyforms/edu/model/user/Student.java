package ru.anyforms.edu.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Клиент курса. current_session_id реализует «одно устройство»:
 * при каждом входе id меняется, JWT со старым id перестаёт действовать.
 */
@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    /** SELF или PERSONAL — тариф из anyforms-back */
    @Column(length = 16)
    private String plan;

    @Column(name = "current_session_id")
    private UUID currentSessionId;

    /** NULL = онбординг ещё не пройден */
    @Column(name = "onboarding_done_at")
    private Instant onboardingDoneAt;

    /** Первый вход или запрос после выдачи доступа; NULL = ни разу не заходил */
    @Column(name = "first_seen_at")
    private Instant firstSeenAt;

    /** Последняя активность: вход или любой запрос с живым JWT (см. SaverStudent#touchSeen) */
    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
