-- ═══════════════════════════════════════════════════════════════════════
-- Онбординг переезжает в отдельную сущность.
-- Раньше слайды собирались из модулей курса, теперь это независимый набор:
-- в админке /admin/onboarding правим слайды, /admin/course — только модули.
-- Синхронизация между ними не нужна.
--
-- Заодно снимаем уникальность (course_id, ord) и (module_id, ord):
-- порядок теперь просто число, а сервис перенумеровывает всё после сохранения.
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE onboarding_slide
(
    id         UUID PRIMARY KEY,
    course_id  UUID        NOT NULL REFERENCES course (id) ON DELETE CASCADE,
    ord        INT         NOT NULL,
    -- TEXT — обычный слайд, SUPPORT — со ссылками чат/поддержка, FINAL — последний («Поехали!»)
    kind       VARCHAR(16) NOT NULL DEFAULT 'TEXT',
    eyebrow    VARCHAR(255),
    -- Слово в {фигурных скобках} выделяется акцентом
    title      TEXT        NOT NULL,
    body       TEXT,
    -- Пункты со стрелками: по одному на строку
    points     TEXT,
    image_url  TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_onboarding_slide_course_id ON onboarding_slide (course_id);

-- ═══════════════ ПЕРЕНОС ТЕКУЩЕГО ОНБОРДИНГА ═══════════════

-- 1. Приветствие
INSERT INTO onboarding_slide (id, course_id, ord, kind, eyebrow, title, body)
SELECT gen_random_uuid(), c.id, 1, 'TEXT', 'Добро пожаловать',
       'Рады видеть вас на курсе {«' || c.title || '»}',
       'Пара минут — расскажем, как здесь всё устроено.'
FROM course c;

-- 2. Сколько модулей
INSERT INTO onboarding_slide (id, course_id, ord, kind, eyebrow, title, body)
SELECT gen_random_uuid(), c.id, 2, 'TEXT', 'Как устроен курс',
       'На курсе будет {' || (SELECT COUNT(*) FROM course_module m WHERE m.course_id = c.id) || ' модуля}',
       'Они открываются по очереди — от простого к сложному.'
FROM course c;

-- 3..N. По слайду на каждый модуль — с его пунктами и картинкой
INSERT INTO onboarding_slide (id, course_id, ord, kind, eyebrow, title, points, image_url)
SELECT gen_random_uuid(), cm.course_id, 2 + cm.ord, 'TEXT',
       'Модуль ' || cm.ord, '{' || cm.title || '}', cm.points, cm.image_url
FROM course_module cm;

-- Предпоследний: мы на связи
INSERT INTO onboarding_slide (id, course_id, ord, kind, eyebrow, title, body)
SELECT gen_random_uuid(), c.id,
       3 + (SELECT COUNT(*) FROM course_module m WHERE m.course_id = c.id),
       'SUPPORT', 'Мы рядом', 'Мы всегда {на связи}',
       'Вопрос по уроку или что-то не работает — пишите, отвечаем быстро.'
FROM course c;

-- Последний: поехали
INSERT INTO onboarding_slide (id, course_id, ord, kind, eyebrow, title, body)
SELECT gen_random_uuid(), c.id,
       4 + (SELECT COUNT(*) FROM course_module m WHERE m.course_id = c.id),
       'FINAL', 'Всё готово', 'Ну что, {поехали?}',
       'Первый модуль уже открыт и ждёт вас.'
FROM course c;

-- ═══════════════ ЧИСТКА МОДУЛЯ ═══════════════
-- points и image_url жили ради онбординга — теперь они его собственные поля
ALTER TABLE course_module DROP COLUMN points;
ALTER TABLE course_module DROP COLUMN image_url;

ALTER TABLE course_module DROP CONSTRAINT uq_course_module_course_ord;
ALTER TABLE lesson DROP CONSTRAINT uq_lesson_module_ord;
