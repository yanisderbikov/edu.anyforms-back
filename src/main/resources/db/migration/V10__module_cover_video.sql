-- Обложка страницы модуля (широкий баннер вверху) и вводное видео модуля.
-- Оба поля — ключ объекта в S3 или полный URL, как у картинки карточки.
ALTER TABLE course_module
    ADD COLUMN cover_url TEXT,
    ADD COLUMN video_url TEXT;
