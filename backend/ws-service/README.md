# ws-service

Реалтайм-шлюз. Один WebSocket на клиента: принимает JSON-команды и шлёт
JSON-события. Источник событий — Kafka (CDC-транзакции и чат). Протокол сделан
максимально «прозрачным», чтобы его было удобно гонять из Postman.

- **Порт:** `8083`, WebSocket-эндпоинт `/ws`
- **Зависимости:** Kafka (консьюмер `bankdemo.public.transactions`, `chat-events`),
  account-service и chat-service (REST)
- **Сборка:** `mvn -f pom.xml spring-boot:run`

## Подключение

```
ws://localhost:8080/ws?token=<JWT>      (через gateway)
ws://localhost:8083/ws?token=<JWT>      (напрямую)
```

JWT можно передать в query-параметре `token` (удобно для Postman) или в заголовке
`Authorization: Bearer <JWT>`. Хэндшейк без валидного токена отклоняется (`401`).
Сразу после подключения сервер шлёт `{"event":"connected", ...}`.

## Команды (client → server)

| Команда | Кто | Описание |
|---------|-----|----------|
| `{"command":"getBalance"}` | USER | Баланс (для менеджера — `{"command":"getBalance","username":"alice"}`). |
| `{"command":"getTransactions"}` | USER | История операций. |
| `{"command":"createChat"}` | USER | Открыть чат с менеджером. |
| `{"command":"closeChat"}` | USER | Закрыть чат. |
| `{"command":"sendMessage","text":"..."}` | USER | Сообщение менеджеру. |
| `{"command":"sendMessage","chatUser":"alice","text":"..."}` | MANAGER | Ответ клиенту. |
| `{"command":"getMessages"}` / `{"command":"getMessages","chatUser":"alice"}` | USER / MANAGER | История чата. |
| `{"command":"getChats"}` | MANAGER | Список чатов. |
| `{"command":"ping"}` | все | `{"event":"pong"}`. |

## События (server → client)

`connected`, `balance`, `noAccount`, `transactions`, `transaction` (live, из CDC),
`messages`, `chatMessage` (live, из Kafka), `chatOpened`, `chatClosed`, `chats`,
`pong`, `error`.

## Маршрутизация

В памяти хранится реестр сессий: `username → сессии` и множество сессий менеджеров.
CDC-транзакция уходит владельцу (`owner_username`); сообщение чата — пользователю
`chatUser` и всем менеджерам, поэтому клиент видит только свой чат и менеджера.

## Конфигурация Kafka (фабрики)

`KafkaConsumerConfig` определяет две типизированные фабрики
`ConcurrentKafkaListenerContainerFactory` (каждая со своим `JsonDeserializer` +
`ErrorHandlingDeserializer`):

- `kafkaListenerContainerFactory` → `TransactionChange` (CDC-топик; поля
  `owner_username`/`created_at` через `@JsonProperty`), `concurrency=1`.
- `chatKafkaListenerContainerFactory` → `ChatMessageEvent` (`chat-events`),
  `concurrency=3`.

Листенеры получают сразу типизированный объект, без `String` и ручного парсинга:

```java
@KafkaListener(topics = "${app.topics.transactions}")
public void onChange(TransactionChange change) { ... }

@KafkaListener(topics = "${app.topics.chat-events}",
               containerFactory = "chatKafkaListenerContainerFactory")
public void onChatEvent(ChatMessageEvent event) { ... }
```

## Переменные окружения

`SPRING_KAFKA_BOOTSTRAP_SERVERS`, `APP_JWT_SECRET`, `APP_INTERNAL_API_KEY`,
`APP_ACCOUNT_SERVICE_URL`, `APP_CHAT_SERVICE_URL`.
