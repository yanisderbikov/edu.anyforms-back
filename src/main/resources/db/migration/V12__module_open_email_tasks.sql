-- ═══════════════════════════════════════════════════════════════════════
-- Письма «модуль открыт»: очередь тасок (паттерн anyforms-back) и отметка
-- на модуле, что об открытии уже объявили.
-- Раннер разгребает очередь пачками (tasks.batch-size писем за
-- tasks.fixed-rate-ms), чтобы не упереться в лимит NotiSend.
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE task
(
    id         UUID PRIMARY KEY,
    type       VARCHAR(255) NOT NULL,
    payload    TEXT,
    status     VARCHAR(255) NOT NULL,
    comment    TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Выборка раннера: NEW-таски своего типа, старые первыми
CREATE INDEX idx_task_type_status_created ON task (type, status, created_at);

-- NULL = об открытии модуля ещё не объявляли (письма не ставились в очередь)
ALTER TABLE course_module
    ADD COLUMN open_email_queued_at TIMESTAMPTZ;

-- Модули, открытые до деплоя, считаем объявленными: иначе после первого
-- запуска студентам разом уйдут письма про давно открытые модули.
-- Модуль, открывающийся сегодня, не трогаем — про него письмо уйти должно.
UPDATE course_module
SET open_email_queued_at = now()
WHERE opens_at IS NULL
   OR opens_at < CURRENT_DATE;
