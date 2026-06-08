# account-service

Счета, балансы, транзакции и оркестрация переводов. Источник изменений для CDC.

- **Порт:** `8082`
- **БД:** PostgreSQL `bankdb` (таблицы `accounts`, `transactions`)
- **Зависимости:** PostgreSQL, Kafka (продюсер `transfer-commands`), auth-service (REST)
- **Сборка:** `mvn -f pom.xml spring-boot:run`

Таблицу `transactions` слушает Debezium — каждый INSERT попадает в топик
`bankdemo.public.transactions`, поэтому списания/пополнения в реальном времени
доходят до клиента через ws-service.

## Эндпоинты

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| GET | `/api/accounts/me` | USER/MANAGER (JWT) | Счёт текущего пользователя. `404`, если счёт не открыт. |
| GET | `/api/accounts/me/transactions` | JWT | Последние операции. |
| POST | `/api/transfers` | JWT | Принимает перевод, кладёт команду в Kafka, отвечает `202`. |
| POST | `/api/manager/accounts` | MANAGER | Открыть счёт клиенту (данные клиента берутся из auth-service). |
| POST | `/api/manager/accounts/topup` | MANAGER | Пополнить баланс (создаёт CREDIT → CDC → WS). |
| GET | `/internal/accounts/{username}` | `X-Internal-Api-Key` | Счёт по логину (для команды `getBalance` в ws-service). |
| GET | `/internal/accounts/{username}/transactions` | internal | История по логину (для `getTransactions`). |
| POST | `/internal/transfers/apply` | internal | Атомарное исполнение перевода (вызывает account-background-service). |

## Безопасность

JWT (роль из claim `role`); `/api/manager/**` требует роли `MANAGER`.
`/internal/**` защищены заголовком `X-Internal-Api-Key`.

## Перевод (sync + async)

`POST /api/transfers` валидирует и публикует `TransferCommand` в `transfer-commands`
(сразу `202`, без списания). Исполнение происходит позже в
`/internal/transfers/apply`: атомарно списывает у отправителя и зачисляет
получателю (если его телефон/счёт есть в банке).

## Конфигурация Kafka (фабрика)

`KafkaConfig` определяет `ProducerFactory` + `KafkaTemplate<String, Object>` с
`JsonSerializer` (`ADD_TYPE_INFO_HEADERS=false`) — `TransferCommand` публикуется
как объект (без ручной сериализации). Топик `transfer-commands` — на 3 партиции.

## Переменные окружения

`SPRING_DATASOURCE_*`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `APP_JWT_SECRET`,
`APP_INTERNAL_API_KEY`, `APP_AUTH_SERVICE_URL`.
