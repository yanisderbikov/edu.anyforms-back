-- ═══════════════════════════════════════════════════════════════════════
-- Прогресс клиента переезжает из localStorage браузера в БД:
-- онбординг и просмотренные уроки видны с любого устройства и не теряются.
-- ═══════════════════════════════════════════════════════════════════════

-- NULL = онбординг ещё не пройден
ALTER TABLE student
    ADD COLUMN onboarding_done_at TIMESTAMPTZ;

-- Полностью просмотренные уроки
CREATE TABLE lesson_progress
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id   UUID        NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    lesson_id    UUID        NOT NULL REFERENCES lesson (id) ON DELETE CASCADE,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_lesson_progress_student_lesson UNIQUE (student_id, lesson_id)
);

CREATE INDEX idx_lesson_progress_student_id ON lesson_progress (student_id);
