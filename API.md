# API учебной платформы

Все админские ручки требуют заголовок `Authorization: Bearer <JWT>` с ролью `ADMIN`.
Токен берётся из `POST /api/auth/verify`. Ошибки всегда возвращаются как `{"message": "текст"}`.

**Курс и онбординг независимы.** Модули (`/admin/course`) — это то, что студент проходит:
названия, описания, даты открытия и уроки с видео. Онбординг (`/admin/onboarding`) — набор
слайдов знакомства, который показывается один раз после первого входа. Синхронизировать их не нужно.

---

## 1. Вход

### `POST /api/auth/request-code`
```json
{ "email": "viaduct-mummy.0f@icloud.com" }
```
Ответ `200`: `{ "message": "Код отправлен на почту" }`

Перед отправкой кода проверяется доступ: сначала своя база (`student`),
и только для незнакомого email — запрос в anyforms-back:

```
GET https://anyforms.ru/api/tech/course-access?email=client@mail.ru
X-Auth-Token: <общий межсервисный токен>

200 { "hasAccess": true, "plan": "PERSONAL", "productCode": "COURSE_PERSONAL" }
```

`plan` (`SELF` / `PERSONAL`) сохраняется в `student.plan`, запись клиента создаётся сама —
дальше доступ живёт в нашей базе, и повторные входы в anyforms-back не ходят.
Возможные ответы: `403` — доступ отключён админом, `404` — покупки нет,
`503` — anyforms-back не ответил (для незнакомого email), `429` — код уже отправлен
(повтор не чаще раза в 30 секунд). Админы из `service_user` эту проверку не проходят.

### `POST /api/auth/verify`
```json
{ "email": "viaduct-mummy.0f@icloud.com", "code": "482915" }
```
Ответ `200`:
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "role": "ADMIN", "email": "viaduct-mummy.0f@icloud.com" }
```

---

## 2. Курс и модули

### `GET /api/admin/course`
Все модули с уроками, включая закрытые.

```json
{
  "course": {
    "id": "molds-course",
    "title": "Производство форм",
    "subtitle": "От идеи до готовой формы",
    "modulesCount": 4
  },
  "support": {
    "chatLabel": "Чат курса",
    "chatUrl": "https://t.me/anyforms_chat",
    "supportLabel": "Поддержка",
    "supportUrl": "https://t.me/AnyFormsBot"
  },
  "modules": [
    {
      "id": "35d552c2-30f1-4c8b-be4a-e154fb3d1c16",
      "order": 1,
      "title": "Проектирование",
      "description": "Проектируем оснастку в Blender",
      "status": "open",
      "opensAt": null,
      "lessons": [
        {
          "id": "9992f83f-3997-4acc-831f-a824f2c30398",
          "title": "Знакомство с Blender",
          "description": "Разбираемся в интерфейсе",
          "videoUrl": "https://edu-anyforms.storage.yandexcloud.net/videos/abc.mp4?X-Amz-Signature=...",
          "videoKey": "videos/abc12345-1111-2222-3333-444455556666.mp4",
          "cover": "https://edu-anyforms.storage.yandexcloud.net/lessons/abc.jpg?X-Amz-Signature=...",
          "coverKey": "lessons/abc12345-1111-2222-3333-444455556666.jpg"
        }
      ]
    }
  ]
}
```

### `GET /api/course` — то, что видит студент
Требует JWT любой роли. У закрытых модулей `lessons` пустой, `videoKey` и `coverKey` всегда `null`.
`opensAt` в ответе — московское время открытия вида `"2026-09-01T14:00"` (или `null`).

### `PUT /api/admin/course` — шапка курса
```json
{
  "title": "Производство форм",
  "subtitle": "От идеи до готовой формы",
  "chatLabel": "Чат курса",
  "chatUrl": "https://t.me/anyforms_chat",
  "supportLabel": "Поддержка",
  "supportUrl": "https://t.me/AnyFormsBot"
}
```
Ответ `204`. Обязательное поле: `title`.

### Модули
- `POST /api/admin/modules` → `201` `{ "id": "..." }`
- `PUT /api/admin/modules/{moduleId}` → `204`
- `DELETE /api/admin/modules/{moduleId}` → `204` (уроки удаляются вместе с модулем)

```json
{
  "order": 1,
  "title": "Проектирование",
  "description": "Описание для карточки на главной и шапки модуля",
  "opensAt": null
}
```

| Поле | Тип | Обязательно | Смысл |
|---|---|---|---|
| `order` | число | да | Порядок; после сохранения список перенумеровывается |
| `title` | строка | да | Название |
| `description` | строка | нет | Карточка на главной + шапка страницы модуля |
| `opensAt` | `"ГГГГ-ММ-ДДTЧЧ:ММ"` или `null` | нет | Московское время открытия; `null` — открыт сразу |

### Уроки
- `POST /api/admin/modules/{moduleId}/lessons` → `201` `{ "id": "..." }`
- `PUT /api/admin/lessons/{lessonId}` → `204`
- `DELETE /api/admin/lessons/{lessonId}` → `204`

```json
{
  "order": 1,
  "title": "Знакомство с Blender",
  "description": "Разбираемся в интерфейсе и готовим рабочее место",
  "videoUrl": "videos/abc12345-1111-2222-3333-444455556666.mp4",
  "coverUrl": "lessons/abc12345-1111-2222-3333-444455556666.jpg"
}
```

| Поле | Тип | Обязательно | Смысл |
|---|---|---|---|
| `order` | число | да | Порядок; после сохранения список перенумеровывается |
| `title` | строка | нет | Новый урок создаётся без названия и заполняется после |
| `description` | строка | нет | Текст под видео |
| `videoUrl` | строка | нет | Ключ в бакете или полный URL |
| `coverUrl` | строка | нет | Обложка 16:9 — превью видео до запуска; ключ или полный URL |

---

## 3. Онбординг

### `GET /api/admin/onboarding`
```json
{
  "slides": [
    {
      "id": "b7e1...",
      "order": 1,
      "kind": "TEXT",
      "eyebrow": "Добро пожаловать",
      "title": "Рады видеть вас на курсе {«Производство форм»}",
      "body": "Пара минут — расскажем, как здесь всё устроено.",
      "points": [],
      "image": null,
      "imageKey": null
    },
    {
      "id": "c2f4...",
      "order": 3,
      "kind": "TEXT",
      "eyebrow": "Модуль 1",
      "title": "{Проектирование}",
      "body": null,
      "points": ["Разберёмся в программе Blender", "Спроектируем оснастку для заливки"],
      "image": "https://edu-anyforms.storage.yandexcloud.net/onboarding/2e66.jpeg?X-Amz-Signature=...",
      "imageKey": "onboarding/2e66459c-ef52-4e0d-982b-6a82fad53423.jpeg"
    }
  ],
  "support": {
    "chatLabel": "Чат курса",
    "chatUrl": "https://t.me/anyforms_chat",
    "supportLabel": "Поддержка",
    "supportUrl": "https://t.me/AnyFormsBot"
  }
}
```

### `GET /api/onboarding` — то, что видит студент
Требует JWT любой роли. Отличие: `imageKey` всегда `null`.

### Слайды
- `POST /api/admin/onboarding/slides` → `201` `{ "id": "..." }`
- `PUT /api/admin/onboarding/slides/{slideId}` → `204`
- `DELETE /api/admin/onboarding/slides/{slideId}` → `204`

```json
{
  "order": 3,
  "kind": "TEXT",
  "eyebrow": "Модуль 1",
  "title": "{Проектирование}",
  "body": "Короткая подпись под заголовком",
  "points": ["Разберёмся в программе Blender", "Спроектируем оснастку для заливки"],
  "imageUrl": "onboarding/2e66459c-ef52-4e0d-982b-6a82fad53423.jpeg"
}
```

| Поле | Смысл |
|---|---|
| `order` | Порядок слайда; после сохранения перенумеровывается |
| `kind` | `TEXT` — обычный, `SUPPORT` — добавляет кнопки чата и поддержки, `FINAL` — последний, кнопка «Поехали!» |
| `eyebrow` | Мелкая надпись капсом над заголовком |
| `title` | Заголовок; часть в `{фигурных скобках}` рисуется акцентным курсивом |
| `body` | Подпись под заголовком |
| `points` | Пункты со стрелками (массив строк) |
| `imageUrl` | Ключ в бакете или полный `https://…` |

Ссылки на чат и поддержку для слайда `SUPPORT` берутся из шапки курса — отдельно в слайде не хранятся.

---

## 3.5 Прогресс пользователя — `/api/me` (JWT, любая роль)

Онбординг и просмотренные уроки хранятся в БД (у клиента — в `student` и `lesson_progress`),
поэтому не теряются и одинаковы на всех устройствах.

- `GET /api/me/progress` →
  ```json
  { "onboardingDone": false, "completedLessonIds": ["9992f83f-..."] }
  ```
  У админов всегда `onboardingDone: true`, уроки не отслеживаются.
- `POST /api/me/onboarding-done` → `204` — клиент дошёл до конца онбординга.
- `POST /api/me/lessons/{lessonId}/complete` → `204` — урок досмотрен
  (идемпотентно, повторный вызов не ошибка; `404`, если урок не существует).

---

## 4. Загрузка файлов напрямую в S3

### `POST /api/admin/presign-upload`
```json
{ "filename": "lesson1.mp4", "contentType": "video/mp4", "prefix": "videos" }
```
Ответ `200`:
```json
{
  "uploadUrl": "https://edu-anyforms.storage.yandexcloud.net/videos/2e66.mp4?X-Amz-Signature=...",
  "key": "videos/2e66459c-ef52-4e0d-982b-6a82fad53423.mp4"
}
```
Браузер делает `PUT uploadUrl` с телом файла и заголовком `Content-Type`, совпадающим с `contentType`
(он входит в подпись). Ссылка живёт 30 минут. Полученный `key` кладём в `videoUrl` или
`coverUrl` урока, `imageUrl` слайда. `prefix` — папка в бакете: `videos`, `lessons`, `onboarding`.

---

## 5. Доступы

Первый админ платформы (`service_user`) заводится вручную в базе:

```sql
INSERT INTO service_user (id, email, role) VALUES (gen_random_uuid(), 'kolya@anyforms.ru', 'ADMIN');
```

Дальше права раздаются из админки: `PATCH /api/admin/students/{id}/role` (см. ниже).

### Клиенты — `/api/admin/students`
`GET` → массив (новые сверху), `GET ?search=часть@email` → поиск по подстроке,
`POST` `{ "email": "client@mail.ru" }` → `201`,
`PATCH /{id}/active` `{ "active": false }` → включить/отключить доступ,
`PATCH /{id}/role` `{ "role": "ADMIN" | "STUDENT" }` → назначить/забрать права админа
(ADMIN создаёт/включает запись в `service_user`, STUDENT гасит её; себе менять нельзя → `403`),
`PATCH /{id}/plan` `{ "plan": "SELF" | "PERSONAL" }` → формат обучения
(тариф берётся из anyforms-back один раз при первом входе, дальше меняется только здесь),
`DELETE /{id}` → `204` (жёсткое удаление вместе с прогрессом — обычно нужен PATCH).

В ответе у каждого клиента есть `role`: `ADMIN`, если по его email есть активная
запись в `service_user`, иначе `STUDENT`.

Отключённый аккаунт (`active = false`) не пускается на платформу и **не
реактивируется покупкой** при входе — вернуть доступ может только админ
через `PATCH { "active": true }`.

Только эти адреса могут получить код входа. У клиента активна одна сессия: вход
с нового устройства гасит старый токен. На админов это не распространяется.

---

## Что стоит помнить

1. **PUT перезаписывает объект целиком.** Не прислали `points` — они очистятся,
   не прислали `opensAt` — модуль откроется. Всегда отправляйте полный объект.
2. **Порядок — просто число.** Поставили уроку `1` — он встанет первым, соседи подвинутся.
   После каждого сохранения сервер перенумеровывает список в 1, 2, 3…, поэтому дырок
   и конфликтов не бывает. Так же работают модули и слайды онбординга.
3. **Читаем `videoUrl`/`cover`/`image`, пишем `videoUrl`/`coverUrl`/`imageUrl` из
   `videoKey`/`coverKey`/`imageKey`.** В ответе `videoUrl`, `cover` и `image` — подписанные
   ссылки на час (для плеера и превью), а сохранять нужно «сырое» значение из
   `videoKey` / `coverKey` / `imageKey`.
4. **`status` считается на лету** из `opensAt` — отправлять его не нужно.
