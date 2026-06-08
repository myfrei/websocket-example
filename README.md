# WS Bank — event-driven microservice demo

Демо «личного кабинета банка» с **ролями (клиент/менеджер)**, **активацией
регистраций менеджером**, **чатом клиент ↔ менеджер** и лентой операций **в
реальном времени**. Показывает связку синхронных (REST) и асинхронных (Kafka)
действий, CDC через Debezium и **понятный WebSocket-протокол на JSON-командах**,
который удобно гонять из Postman.

## Что нового по сравнению с базовой версией

- 👤 **Роли**: `USER` и `MANAGER` (логин менеджера `man / man123`).
- ✅ **Активация**: регистрация создаёт запись со статусом `PENDING`; войти можно
  только после подтверждения менеджером (иначе — «Ожидайте активации»).
- 🏦 **Менеджер** открывает счета клиентам и пополняет баланс.
- 💬 **Чат** клиент ↔ менеджер (через Kafka-топик `chat-events`); клиент видит
  только свою переписку и менеджера, менеджер — все чаты.
- 🔌 **WebSocket = JSON-команды**: `{"command":"getBalance"}` → `{"event":"balance",...}`.
  Все события видны в Postman; всё можно делать «руками» (REST + Postman + Kafka UI),
  не только через фронт.

## Архитектура

```
                      React SPA (USER / MANAGER)
                  REST  +  WebSocket (JSON-команды)
                               │ :8080
                        ┌──────▼───────┐
                        │  api-gateway │   маршрутизация + CORS
                        └───┬─────┬────┘
        /api/auth, users    │     │  /api/accounts, /api/transfers,
                            ▼     ▼  /api/manager/accounts          /ws
                  ┌──────────────┐  ┌──────────────┐        ┌──────────────┐
                  │ auth-service │  │account-service│        │  ws-service  │
                  │   authdb     │  │    bankdb     │        │ команды/JSON │
                  └──────────────┘  └──┬────────┬──┘        └───▲──────▲───┘
                                       │        │ Debezium CDC   │      │
                       transfer-commands│        ▼  bankdemo.public.    │ chat-events
                              (Kafka)   │   transactions (Kafka)─┘      │ (Kafka)
                                        ▼                               │
                          ┌────────────────────────┐         ┌──────────┴───┐
                          │account-background-service│        │ chat-service │
                          │  Kafka → REST apply      │        │    chatdb    │
                          └────────────┬─────────────┘        └──────────────┘
                                       └──REST──▶ account-service
```

- **Синхронно (REST):** логин, чтение счёта, действия менеджера.
- **Асинхронно (Kafka):** перевод (`transfer-commands`) и чат (`chat-events`).
- **CDC (Debezium):** изменения `transactions` → топик → ws-service → WebSocket.
  Подробный разбор работы Debezium на примере проекта — в [`infra/README.md`](infra/README.md).

Подробно по каждому сервису — в его `README.md` (см. таблицу [Сервисы](#сервисы)).

## Технологии

Java 21 · Spring Boot 3.3 · Spring Cloud Gateway · Spring Security (JWT) ·
Spring Data JPA · Spring Kafka · Spring WebSocket · PostgreSQL 16 (logical
replication) · Apache Kafka 7.6 (KRaft) · Debezium 2.7 · React 18 · Vite ·
TypeScript · Docker Compose.

## Быстрый старт

Нужен только Docker (Compose v2+).

```bash
docker compose up -d --build
docker compose ps
```

Откройте **http://localhost:3000**. Остановить и удалить данные:
`docker compose down -v`.

## Адреса и демо-доступы

| Что | URL |
|-----|-----|
| 🖥️ Фронтенд | http://localhost:3000 |
| 🚪 API Gateway | http://localhost:8080 |
| 📊 Kafka UI | http://localhost:8090 |
| 🔌 Debezium Connect | http://localhost:8085/connectors |

| Логин | Пароль | Роль |
|-------|--------|------|
| `man` | `man123` | MANAGER |
| `alice` | `alice123` | USER (есть счёт) |
| `bob` | `bob123` | USER (есть счёт) |

## Сценарий демонстрации

1. **Регистрация и активация.** Зарегистрируйте нового пользователя — увидите
   «Ожидайте активации менеджером», войти пока нельзя. Войдите как `man`,
   во вкладке «Заявки на регистрацию» нажмите «Активировать». Теперь новый
   пользователь может войти.
2. **Открытие счёта и пополнение.** Под менеджером откройте счёт новому
   пользователю и пополните баланс — клиент мгновенно увидит операцию в ленте.
3. **Перевод в реальном времени.** Войдите как `alice` (обычное окно) и `bob`
   (инкогнито). Переведите от alice на телефон bob `+79990000002` — у обоих
   лента и баланс обновятся без перезагрузки.
4. **Чат.** В кабинете клиента напишите менеджеру; под `man` откройте чат и
   ответьте — сообщения доходят мгновенно (через Kafka `chat-events`).

## WebSocket-протокол (Postman / backend)

Подключение (через gateway): `ws://localhost:8080/ws?token=<JWT>`.

Получить JWT:

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"alice123"}'
# -> { "token": "...", "role": "USER", ... }
```

В **Postman**: New → WebSocket → URL `ws://localhost:8080/ws?token=<JWT>` →
Connect. Отправляйте JSON и видите все входящие/исходящие кадры:

```json
{"command":"getBalance"}
{"command":"getTransactions"}
{"command":"createChat"}
{"command":"sendMessage","text":"Здравствуйте"}
{"command":"closeChat"}
{"command":"ping"}
```

Менеджер дополнительно: `{"command":"getChats"}`,
`{"command":"getMessages","chatUser":"alice"}`,
`{"command":"sendMessage","chatUser":"alice","text":"Чем помочь?"}`.

События сервера: `connected`, `balance`, `noAccount`, `transactions`,
`transaction` (live), `messages`, `chatMessage` (live), `chatOpened`,
`chatClosed`, `chats`, `pong`, `error`. Полный список — в
[`backend/ws-service/README.md`](backend/ws-service/README.md).

## Демонстрация через Kafka UI

В http://localhost:8090 → Topics видны все события:

- `transfer-commands` — команды переводов (account-service → background).
- `bankdemo.public.transactions` — поток изменений из Postgres (Debezium CDC).
- `chat-events` — сообщения чата (chat-service → ws-service).

Сделайте перевод/сообщение и обновите топик — увидите новое сообщение.

## Действия менеджера через REST (curl / Postman)

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' -d '{"username":"man","password":"man123"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')

curl -s http://localhost:8080/api/manager/users/pending -H "Authorization: Bearer $TOKEN"
curl -s -X POST http://localhost:8080/api/manager/users/<username>/approve -H "Authorization: Bearer $TOKEN"
curl -s -X POST http://localhost:8080/api/manager/accounts -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"username":"<username>","currency":"RUB","initialBalance":5000}'
curl -s -X POST http://localhost:8080/api/manager/accounts/topup -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"username":"<username>","amount":2500,"description":"Бонус"}'
```

## Сервисы

| Сервис | Порт | Роль | README |
|--------|------|------|--------|
| `api-gateway` | 8080 | Единая точка входа, REST + WebSocket, CORS | [↗](backend/api-gateway/README.md) |
| `auth-service` | 8081 | Пользователи, роли, JWT, активация | [↗](backend/auth-service/README.md) |
| `account-service` | 8082 | Счета, транзакции, переводы, операции менеджера | [↗](backend/account-service/README.md) |
| `account-background-service` | — | Kafka-консьюмер переводов → REST | [↗](backend/account-background-service/README.md) |
| `chat-service` | 8084 | Чат: БД + публикация в Kafka | [↗](backend/chat-service/README.md) |
| `ws-service` | 8083 | WebSocket: JSON-команды и события | [↗](backend/ws-service/README.md) |
| `frontend` | 3000 | React SPA | [↗](frontend/README.md) |

## Структура репозитория

```
.
├── docker-compose.yml
├── .env.example
├── infra/
│   ├── postgres/init.sql            # authdb, bankdb, chatdb
│   └── debezium/account-connector.json
├── backend/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── account-service/
│   ├── account-background-service/
│   ├── chat-service/
│   └── ws-service/
└── frontend/
```

## Локальная разработка (без Docker)

```bash
docker compose up -d postgres kafka connect kafka-ui
mvn -f backend/account-service/pom.xml spring-boot:run
cd frontend && npm install && npm run dev   # http://localhost:5173
```

Дефолты в `application.yml` рассчитаны на `localhost`; переменные окружения в
`docker-compose.yml` переопределяют их на имена сервисов Docker.

## Диагностика

```bash
docker compose ps
docker compose logs -f ws-service
curl http://localhost:8085/connectors/account-connector/status
```

- **Коннектор не RUNNING:** `docker compose up -d connector-init` (он ждёт, пока
  account-service станет healthy, и регистрирует Debezium-коннектор).
- **WebSocket не подключается:** проверьте, что в URL есть `?token=<JWT>` и токен
  свежий; логи `ws-service` показывают `WS connected: '<user>'`.
- **Новый пользователь не может войти:** его ещё не активировал менеджер.

## Упрощения демо

- Один Postgres с тремя БД (`authdb`, `bankdb`, `chatdb`) вместо отдельных серверов.
- Нет конвертации валют (перевод исполняется при совпадении валюты со счётом).
- ws-service хранит сессии в памяти (один инстанс); для масштабирования нужен
  общий реестр/sticky-сессии.
- Секреты (`JWT_SECRET`, `INTERNAL_API_KEY`) заданы по умолчанию в compose —
  для реального использования вынесите в `.env`.
```
