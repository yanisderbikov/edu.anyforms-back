-- ═══════════════════════════════════════════════════════════════════════
-- Активность клиента: когда впервые и когда в последний раз был на платформе.
-- Отмечает JwtAuthFilter по любому запросу студента с живым JWT (запись
-- не чаще раза в несколько минут) и вход по коду. NULL в first_seen_at =
-- после выдачи доступа ни разу не заходил. Админов не трекаем: они не студенты.
-- ═══════════════════════════════════════════════════════════════════════

ALTER TABLE student
    ADD COLUMN first_seen_at TIMESTAMPTZ,
    ADD COLUMN last_seen_at  TIMESTAMPTZ;

-- Бэкфилл из истории кодов входа: иначе все, кто заходил до деплоя, стали бы
-- «ни разу не заходил». Точный признак «входил хоть раз» — current_session_id:
-- он ставится только при успешном вводе кода. Моменты берём по кодам с used = true
-- приблизительно: used ставится и при успешном входе, и при запросе нового кода
-- вместо старого, так что первый/последний код могут отличаться от настоящего
-- входа на несколько минут. Дальше поля ведутся точно.
UPDATE student s
SET first_seen_at = lc.first_at,
    last_seen_at  = lc.last_at
FROM (SELECT email, MIN(created_at) AS first_at, MAX(created_at) AS last_at
      FROM login_code
      WHERE used
      GROUP BY email) lc
WHERE lc.email = s.email
  AND s.current_session_id IS NOT NULL;
