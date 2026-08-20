-- Мягкое удаление урока: строка остаётся, чтобы вместе с ней не пропал прогресс
-- студентов (lesson_progress ссылается на lesson с ON DELETE CASCADE).
-- Урок с проставленным deleted_at не виден нигде — фильтр стоит на самой сущности.
-- Файлы урока при удалении стираются безвозвратно (см. LessonAssetCleaner):
-- восстановить строку можно, видео и материалы придётся заливать заново.
ALTER TABLE lesson
    ADD COLUMN deleted_at TIMESTAMPTZ;

-- Живые уроки модуля читаются на каждой странице курса
CREATE INDEX idx_lesson_alive ON lesson (module_id) WHERE deleted_at IS NULL;
