-- Текст под заголовком блока обратной связи в конце модуля; пусто = без текста.
ALTER TABLE course_module
    ADD COLUMN feedback_description TEXT;
