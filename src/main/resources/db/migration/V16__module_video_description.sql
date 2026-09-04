-- Второе описание модуля: текст под вводным видео на странице модуля.
-- Прежнее description остаётся превью карточки на главном экране.
-- Сейчас в обоих местах показывается один и тот же текст — копируем его
-- в новое поле, чтобы после деплоя страницы модулей не опустели.
ALTER TABLE course_module
    ADD COLUMN video_description TEXT;

UPDATE course_module
SET video_description = description;
