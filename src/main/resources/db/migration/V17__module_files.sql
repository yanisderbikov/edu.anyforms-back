-- Файлы-материалы модуля: то же, что материалы урока (V8), но показываются
-- под описанием модуля. file_url — ключ объекта в S3 или полный URL,
-- name — исходное имя файла, под ним студент его скачает.
-- Модуль удаляется жёстко, поэтому его файлы уходят каскадом вместе с ним.
CREATE TABLE module_file
(
    id         UUID PRIMARY KEY,
    module_id  UUID         NOT NULL REFERENCES course_module (id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    file_url   TEXT         NOT NULL,
    size_bytes BIGINT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_module_file_module_id ON module_file (module_id);
