# edu.anyforms-back

Бэкенд учебной платформы anyforms. Стек и паттерны — как в anyforms-5:
Spring Boot 3.2 / Java 21 / Maven, PostgreSQL + Flyway, S3 (Yandex Object Storage),
слои Getter/Saver → Manager → Service → Controller.

## Запуск локально

```bash
docker compose up -d postgres     # Postgres на localhost:5460
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # ВАЖНО: только JDK 21, на 25 ломается Lombok
mvn clean package -DskipTests
set -a && source .env && set +a
java -jar target/edu-anyforms-back-1.0-SNAPSHOT.jar
```

- API: http://localhost:8091
- Swagger: http://localhost:8091/swagger-ui.html

## Что есть

- **Авторизация по коду с почты** (NotiSend, как в anyforms-5):
  - `POST /api/auth/request-code` `{email}` — код на почту (TTL 10 мин, 5 попыток,
    повтор не чаще раза в минуту).
  - **Доступ клиента проверяется в anyforms-5**: `GET /api/tech/course-access?email=…`
    (заголовок `X-Auth-Token` = общий межсервисный токен). Там ищутся оплаченные
    (`SUCCEEDED`) транзакции с продуктами `COURSE` / `COURSE_PERSONAL`. Ответ
    `{hasAccess, plan}` сохраняется в `student.plan` (`SELF` / `PERSONAL`), запись
    клиента создаётся автоматически. Админы (`service_user`) проверку не проходят.
    Если anyforms-5 не ответил — пускаем тех, кто уже заходил раньше; новым отдаём 503.
  - `POST /api/auth/verify` `{email, code}` → `{token, role, email}`. JWT живёт месяц.
  - **Одно устройство для клиентов**: новый вход меняет `student.current_session_id`,
    старые токены гаснут. На админов не распространяется.
  - Без `EMAIL_NOTISEND_API_KEY` письмо не шлётся — код виден в логе бэкенда (удобно локально).
  - Шаблон письма: `resources/templates/email-login-code.html` (тёмный дизайн курса).
- **API курса** — `GET /api/course` (требуется JWT, любая роль): курс + модули + уроки одним JSON.
  Уроки закрытых модулей наружу не отдаются.
- **API онбординга** — `GET /api/onboarding` (JWT): слайды знакомства. Независим от модулей курса.
- **Админка (API)** — `/api/admin/**`, только роль ADMIN. Полное описание с примерами — в [API.md](API.md).
  В Swagger — кнопка Authorize (Bearer JWT):
  - `GET /api/admin/course` — всё, включая закрытые модули
  - `PUT /api/admin/course` — шапка курса и ссылки поддержки
  - `POST/PUT/DELETE /api/admin/modules[/{id}]` — модули (points — списком, opensAt: null = открыт)
  - `POST /api/admin/modules/{id}/lessons`, `PUT/DELETE /api/admin/lessons/{id}` — уроки
  - `GET /api/admin/onboarding`, `POST/PUT/DELETE .../slides[/{id}]` — слайды онбординга
  - `GET/POST/DELETE /api/admin/students` — email'ы клиентов (выдача/отзыв доступа к курсу)

  Файлы грузятся из админки кнопками, минуя бэкенд: видео — в Kinescope
  (`POST /api/admin/kinescope/upload-link`), картинки и материалы — в S3
  (`POST /api/admin/presign-upload`). Ссылки руками не вводятся.
- **Миграция V1** — схема + сиды: курс «Производство форм», 4 модуля с пунктами
  (Проектирование / Печать / Ручная обработка / Заливка силикона), админский email.

## Решения

- **JSON не храним в БД** — нормализованные таблицы `course` / `course_module` / `lesson`:
  админка редактирует поля по отдельности, публичный JSON собирается в сервисе.
  Обновления ~раз в неделю, чтение ежедневное — Postgres справляется без кеша.
- **`points`** (пункты «что внутри» для онбординга) — TEXT, по одному пункту на строку.
- **`video_url` / `image_url`** — либо полный `http(s)`-URL (отдаётся как есть),
  либо ключ S3 (превращается в presigned URL на час).
- **S3 опционален**: без `S3_ACCESS_KEY_ID` приложение стартует, но `/api/admin/upload` вернёт 503.
  Бакет свой — `edu-anyforms` (не общий с anyforms-5).
- **Видео уроков — в Kinescope, не в S3** (S3 остался под обложки, файлы уроков и картинки).
  В `video_url` лежит `https://kinescope.io/embed/{id}`; загрузка идёт из браузера прямо
  в Kinescope по одноразовой ссылке от `POST /api/admin/kinescope/upload-link`.
  Защиту (домены, DRM, вотермарка) настраивает кабинет Kinescope; от нас — email студента
  в вотермарке и токен воспроизведения: `GET /api/course/video-token` выдаёт короткий JWT
  (claim `purpose=kinescope-drm`), Kinescope проверяет его через `POST /api/kinescope/drm-auth`
  перед выдачей ключей. Колбэк регистрируется однократно:
  `PUT https://api.kinescope.io/v1/drm/auth`. Без `KINESCOPE_API_TOKEN` приложение стартует,
  но загрузка видео вернёт 503.
- **Удаление урока — мягкое**: проставляется `lesson.deleted_at`, строка остаётся,
  иначе каскадом уехал бы прогресс студентов (`lesson_progress` → `ON DELETE CASCADE`).
  Из всех выборок такой урок исчезает сам — фильтр висит на сущности (`@SQLRestriction`).
- **Файлы подчищаются во внешних хранилищах** (`LessonAssetCleaner`): при удалении урока
  или модуля и при замене видео/обложки/картинки старое уходит из Kinescope и S3.
  Удаления параллельны и стартуют **только после коммита** транзакции — откатить их
  вместе с ней невозможно, а «файла нет, а урок на месте» хуже, чем файл-сирота.
  Перед удалением проверяется, не ссылается ли на тот же файл кто-то ещё.
- Модуль «открыт», если `opens_at IS NULL` или дата наступила — статус считается на лету.

## Подключение фронтенда

В edu.anyforms-front настроен прокси в vite.config.js:

```js
server: { proxy: { '/api': 'http://localhost:8091' } }
```

## Админы платформы

Список ведётся вручную в таблице `service_user` — API для него нет:

```sql
INSERT INTO service_user (id, email, role) VALUES (gen_random_uuid(), 'kolya@anyforms.ru', 'ADMIN');
```

Такой email проверку покупки в anyforms-5 не проходит и получает роль ADMIN.
