# api-gateway

Единая точка входа (Spring Cloud Gateway, reactive). Маршрутизирует REST и
WebSocket, централизованно отдаёт CORS. Браузер общается только с ним (`:8080`).

- **Порт:** `8080`
- **Сборка:** `mvn -f pom.xml spring-boot:run`

## Маршруты

| Путь | Назначение |
|------|------------|
| `/api/auth/**`, `/api/manager/users/**` | auth-service `:8081` |
| `/api/accounts/**`, `/api/transfers/**`, `/api/manager/accounts/**` | account-service `:8082` |
| `/ws/**` | ws-service `:8083` (апгрейд до WebSocket выполняется автоматически) |

`/internal/**` через gateway не проксируются — это сугубо межсервисные вызовы.

## CORS

`globalcors` разрешает любой origin (демо), `allowCredentials=false` — авторизация
идёт через `Authorization: Bearer`, без cookie.

## Переменные окружения

`APP_AUTH_SERVICE_URI`, `APP_ACCOUNT_SERVICE_URI`, `APP_WS_SERVICE_URI`.
