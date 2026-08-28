-- Картинка модуля (16:9) для карточки на главном экране.
-- Ключ объекта в S3 или полный URL — как у видео уроков.
ALTER TABLE course_module
    ADD COLUMN image_url TEXT;
