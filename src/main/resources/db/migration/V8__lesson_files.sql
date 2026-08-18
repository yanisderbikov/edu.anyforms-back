-- Файлы-материалы урока: методички, чек-листы, исходники.
-- Количество не ограничено; file_url — ключ объекта в S3 или полный URL,
-- как у видео урока. name — исходное имя файла, под ним студент его скачает.
CREATE TABLE lesson_file
(
    id         UUID PRIMARY KEY,
    lesson_id  UUID         NOT NULL REFERENCES lesson (id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    file_url   TEXT         NOT NULL,
    size_bytes BIGINT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_lesson_file_lesson_id ON lesson_file (lesson_id);
