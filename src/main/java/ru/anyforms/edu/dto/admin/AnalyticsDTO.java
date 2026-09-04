package ru.anyforms.edu.dto.admin;

import java.time.Instant;
import java.util.List;

/**
 * Ответ /api/admin/analytics/students: воронка прогресса по каждому клиенту.
 * Все моменты — ISO-8601 в UTC; фронт показывает их по Москве.
 * Сортировка и фильтры делаются на фронте: клиентов сотни, один ответ проще пагинации.
 */
public record AnalyticsDTO(
        Instant generatedAt,
        List<ModuleInfo> modules,
        List<StudentRow> students
) {

    /** Заголовок колонки модуля */
    public record ModuleInfo(String id, int order, String title, int lessonsCount, boolean open, Instant opensAt) {
    }

    public record StudentRow(
            String id,
            String email,
            String plan,
            boolean active,
            Instant createdAt,
            Instant firstSeenAt,
            Instant lastSeenAt,
            Instant onboardingDoneAt,
            /** Позднейшее из: запрос с JWT, онбординг, заход в модуль, старт и завершение урока */
            Instant lastActivityAt,
            /** Уроков в открытых модулях — знаменатель общего прогресса */
            int lessonsAvailable,
            /** Начатых уроков, включая досмотренные */
            int lessonsStarted,
            int lessonsDone,
            Stage stage,
            /** В порядке модулей курса, как в modules */
            List<ModuleProgress> modules
    ) {
    }

    /**
     * Ступень воронки. Для ступеней внутри курса указан модуль, на котором
     * клиент остановился: первый открытый модуль с уроками, который не пройден.
     */
    public record Stage(StageKind kind, Integer moduleOrder, String moduleTitle) {
    }

    public enum StageKind {
        /** После выдачи доступа ни разу не заходил */
        NEVER_SEEN,
        /** Заходил, но онбординг не прошёл */
        ONBOARDING,
        /** Онбординг пройден, страницу модуля не открывал */
        NOT_OPENED,
        /** Модуль открывал, ни одного видео не запускал */
        OPENED,
        /** Запускал видео, ни одного урока не досмотрел */
        STARTED,
        /** Часть уроков модуля досмотрена */
        IN_PROGRESS,
        /** Всё открытое пройдено, следующий модуль ещё закрыт */
        WAITING_NEXT,
        /** Пройдены все модули курса */
        COMPLETED
    }

    public enum ModuleState {
        /** Модуль ещё не открылся */
        LOCKED,
        NOT_OPENED,
        OPENED,
        STARTED,
        IN_PROGRESS,
        DONE
    }

    public record ModuleProgress(
            String moduleId,
            ModuleState state,
            int lessonsCount,
            /** Начатых уроков, включая досмотренные */
            int lessonsStarted,
            int lessonsDone,
            /** Сколько раз страница модуля загружалась с сервера */
            int visits,
            Instant firstVisitedAt,
            Instant lastVisitedAt,
            Instant firstStartedAt,
            Instant lastCompletedAt,
            /** Позднейшее из: заход, старт и завершение урока */
            Instant lastActivityAt
    ) {
    }
}
