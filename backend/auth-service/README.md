# auth-service

Аутентификация, пользователи, роли и активация менеджером.

- **Порт:** `8081`
- **БД:** PostgreSQL `authdb` (таблица `users`)
- **Зависимости:** PostgreSQL
- **Сборка:** `mvn -f pom.xml spring-boot:run`

## Модель

`User`: `username`, `passwordHash` (BCrypt), `fullName`, `phone`, `role` (`USER` | `MANAGER`),
`status` (`PENDING` | `ACTIVE`).

Регистрация создаёт пользователя со статусом **PENDING** — войти он сможет только
после того, как менеджер его активирует.

## Эндпоинты

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| POST | `/api/auth/register` | публичный | Создаёт `PENDING`-пользователя. Токен не выдаётся. |
| POST | `/api/auth/login` | публичный | Проверяет пароль и статус. `PENDING` → `403 «Ожидайте активации менеджером»`. Возвращает JWT (с claim `role`). |
| GET | `/api/manager/users` | MANAGER | Все пользователи. |
| GET | `/api/manager/users/pending` | MANAGER | Заявки, ожидающие активации. |
| POST | `/api/manager/users/{username}/approve` | MANAGER | Переводит пользователя в `ACTIVE`. |
| GET | `/internal/users/{username}` | `X-Internal-Api-Key` | Данные пользователя (используется account-service при открытии счёта). |

## JWT

Подписывается HS-секретом `APP_JWT_SECRET` (общий для всех сервисов). Claims:
`sub` (username), `fullName`, `phone`, `role`. account-service и ws-service
валидируют токен этим же секретом.

## Сиды

`man / man123` (MANAGER, ACTIVE), `alice / alice123`, `bob / bob123` (USER, ACTIVE).

## Переменные окружения

`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`,
`APP_JWT_SECRET`, `APP_INTERNAL_API_KEY`.
