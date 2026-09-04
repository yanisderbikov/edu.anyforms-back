-- ═══════════════════════════════════════════════════════════════════════
-- Аналитика прогресса: чтобы в админке было видно, кто до какого места дошёл.
-- 1. Заходы на страницу модуля: отличаем «открыл, но не начал» от «не открывал».
-- 2. Старт урока: lesson_progress теперь одна строка на пару студент+урок —
--    started_at (первый запуск видео) и completed_at (досмотрел; NULL = ещё нет).
-- Активность студента (first_seen_at / last_seen_at) — в V14.
-- ═══════════════════════════════════════════════════════════════════════

-- Одна строка на пару студент+модуль: первый и последний заход плюс счётчик.
-- Пишется upsert'ом в фоне (ActivityTracker), запрос студента БД не ждёт.
CREATE TABLE module_visit
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id       UUID        NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    module_id        UUID        NOT NULL REFERENCES course_module (id) ON DELETE CASCADE,
    first_visited_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_visited_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    visits           INT         NOT NULL DEFAULT 1,
    CONSTRAINT uq_module_visit_student_module UNIQUE (student_id, module_id)
);

-- Старт урока. Старые строки — только досмотренные, поэтому старт = завершение.
ALTER TABLE lesson_progress
    ADD COLUMN started_at TIMESTAMPTZ;

UPDATE lesson_progress
SET started_at = completed_at;

ALTER TABLE lesson_progress
    ALTER COLUMN started_at SET NOT NULL,
    ALTER COLUMN started_at SET DEFAULT now(),
    ALTER COLUMN completed_at DROP NOT NULL,
    ALTER COLUMN completed_at DROP DEFAULT;
