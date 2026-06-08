# chat-service

Хранит переписку «пользователь ↔ менеджер» и публикует сообщения в Kafka, откуда
их разбирает ws-service и доставляет по WebSocket.

- **Порт:** `8084`
- **БД:** PostgreSQL `chatdb` (таблицы `chats`, `chat_messages`)
- **Зависимости:** PostgreSQL, Kafka (продюсер `chat-events`)
- **Сборка:** `mvn -f pom.xml spring-boot:run`

У каждого пользователя один чат с менеджером (`chats.user_username` уникален).

## Эндпоинты (только internal, заголовок `X-Internal-Api-Key`)

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/internal/chats/{username}/open` | Открыть (или вернуть) чат пользователя. |
| POST | `/internal/chats/{username}/close` | Закрыть чат. |
| POST | `/internal/chats/{username}/messages` | Сохранить сообщение и опубликовать в `chat-events`. Тело: `{fromUsername, fromRole, text}`. |
| GET | `/internal/chats/{username}/messages` | История сообщений чата. |
| GET | `/internal/chats` | Список всех чатов (для менеджера). |

## Событие `chat-events`

```json
{ "chatId": "...", "chatUser": "alice", "fromUsername": "man",
  "fromRole": "MANAGER", "text": "Здравствуйте", "createdAt": "2026-..." }
```

Эти сообщения видны в Kafka UI и доставляются ws-service пользователю `chatUser`
и всем менеджерам.

## Конфигурация Kafka (фабрика)

`KafkaConfig` определяет `ProducerFactory` + `KafkaTemplate<String, Object>` с
`JsonSerializer` (`ADD_TYPE_INFO_HEADERS=false`) — `ChatMessageEvent` публикуется
как объект. Топик `chat-events` — на 3 партиции.

## Переменные окружения

`SPRING_DATASOURCE_*`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `APP_INTERNAL_API_KEY`.
