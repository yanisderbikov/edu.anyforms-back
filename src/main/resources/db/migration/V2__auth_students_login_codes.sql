-- ═══════════════════════════════════════════════════════════════════════
-- Авторизация по email-коду.
-- student — клиенты курса (доступ выдаёт админ). current_session_id —
-- «одно устройство»: при новом входе id меняется и старые JWT протухают.
-- На админов (service_user) эта логика не распространяется.
-- login_code — одноразовые коды: TTL 10 минут, максимум 5 попыток ввода.
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE student
(
    id                 UUID PRIMARY KEY,
    email              VARCHAR(255) NOT NULL UNIQUE,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    current_session_id UUID,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE login_code
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    code       VARCHAR(6)   NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    attempts   INT          NOT NULL DEFAULT 0,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_login_code_email ON login_code (email);
