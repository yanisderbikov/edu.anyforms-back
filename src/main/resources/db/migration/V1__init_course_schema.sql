-- ═══════════════════════════════════════════════════════════════════════
-- Учебная платформа anyforms: базовая схема.
-- Курс → модули → уроки. Нормализованные таблицы (без JSON в БД):
-- админка редактирует поля по отдельности, обновления редкие (раз в неделю),
-- а публичный JSON собирается на лету в сервисе — при таких нагрузках
-- (ежедневные чтения) этого более чем достаточно.
-- service_user — сервисные пользователи (админы), вход по email позже.
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE course
(
    id          UUID PRIMARY KEY,
    slug        VARCHAR(64)  NOT NULL UNIQUE,
    title       VARCHAR(255) NOT NULL,
    subtitle    TEXT,
    chat_label  VARCHAR(64)  NOT NULL DEFAULT 'Чат курса',
    chat_url    TEXT,
    support_label VARCHAR(64) NOT NULL DEFAULT 'Поддержка',
    support_url TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE course_module
(
    id          UUID PRIMARY KEY,
    course_id   UUID         NOT NULL REFERENCES course (id) ON DELETE CASCADE,
    ord         INT          NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    -- Пункты «что внутри» для онбординга: по одному на строку
    points      TEXT,
    -- Ключ картинки в S3 или полный URL (если начинается с http)
    image_url   TEXT,
    -- NULL = модуль открыт; дата в будущем = «Откроется N числа»
    opens_at    DATE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_course_module_course_ord UNIQUE (course_id, ord)
);

CREATE INDEX idx_course_module_course_id ON course_module (course_id);

CREATE TABLE lesson
(
    id          UUID PRIMARY KEY,
    module_id   UUID         NOT NULL REFERENCES course_module (id) ON DELETE CASCADE,
    ord         INT          NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    -- Ключ видео в S3 или полный URL (если начинается с http)
    video_url   TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_lesson_module_ord UNIQUE (module_id, ord)
);

CREATE INDEX idx_lesson_module_id ON lesson (module_id);

-- Сервисные пользователи: заходят через почту (код на email), логин-сервис позже
CREATE TABLE service_user
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    role       VARCHAR(32)  NOT NULL DEFAULT 'ADMIN',
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ═══════════════ СИДЫ ═══════════════

INSERT INTO course (id, slug, title, subtitle, chat_url, support_url)
VALUES (gen_random_uuid(), 'molds-course', 'Производство форм',
        'От идеи до готовой формы: проектирование, печать, обработка и заливка силикона',
        'https://t.me/anyforms_chat', 'https://t.me/AnyFormsBot');

-- 4 модуля; ссылки на видео и картинки прикрепляются через админку
INSERT INTO course_module (id, course_id, ord, title, description, points, opens_at)
VALUES (gen_random_uuid(), (SELECT id FROM course WHERE slug = 'molds-course'), 1,
        'Проектирование',
        'Проектируем оснастку в Blender: от мастер-модели до удобной рабочей формы.',
        E'Разберёмся в программе Blender\nСпроектируем профессиональную оснастку для заливки силиконом\nСпроектируем конечную удобную оснастку для работы с формой',
        NULL),
       (gen_random_uuid(), (SELECT id FROM course WHERE slug = 'molds-course'), 2,
        'Печать',
        'Печатаем мастер-модель и оснастку: SLA, FDM, оборудование и настройки.',
        E'SLA для мастер-модели, FDM для оснастки\nКакое оборудование подойдёт\nНастройки печати и как ускорить процесс',
        DATE '2026-09-01'),
       (gen_random_uuid(), (SELECT id FROM course WHERE slug = 'molds-course'), 3,
        'Ручная обработка',
        'Доводим мастер-модель до идеала: расходники, приёмы и критерии качества.',
        E'Как обработать мастер-модель\nКакие расходники использовать\nЧто считается приемлемым результатом в зависимости от задачи',
        DATE '2026-09-15'),
       (gen_random_uuid(), (SELECT id FROM course WHERE slug = 'molds-course'), 4,
        'Заливка силикона',
        'Заливаем форму: подбор силикона, управление схватыванием и оборудование.',
        E'Подбор силикона\nКак ускорить / замедлить процесс схватывания\nПодбор оборудования: весы и камера дегазации',
        DATE '2026-10-01');

-- Сервисные пользователи (админы): добавить коллег можно через админ-API
INSERT INTO service_user (id, email, role)
VALUES (gen_random_uuid(), 'viaduct-mummy.0f@icloud.com', 'ADMIN');
