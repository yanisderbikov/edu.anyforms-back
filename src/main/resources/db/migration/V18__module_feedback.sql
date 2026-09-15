-- Кнопка обратной связи в конце модуля: заголовок над кнопкой, подпись
-- на кнопке и ссылка (форма, чат — что угодно). Заполняется в админке на
-- каждом модуле; без ссылки блок не показывается. Пустой заголовок или
-- подпись — фронт подставит текст по умолчанию.
ALTER TABLE course_module
    ADD COLUMN feedback_title VARCHAR(255),
    ADD COLUMN feedback_label VARCHAR(64),
    ADD COLUMN feedback_url   TEXT;
