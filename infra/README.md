# infra — инфраструктура и Debezium (CDC)

Здесь лежит то, что не является сервисом, но нужно для запуска:

```
infra/
├── postgres/init.sql                 # создаёт authdb, bankdb, chatdb
└── debezium/account-connector.json   # конфиг Debezium-коннектора (Postgres → Kafka)
```

Этот файл объясняет, **как работает Debezium на примере нашего проекта**: как
изменение строки в Postgres само «доезжает» до браузера по WebSocket.

## Зачем здесь Debezium

В проекте есть два способа узнать об операции по счёту:

1. **Синхронно** — клиент сам спросит REST (`GET /api/accounts/me/transactions`).
2. **В реальном времени** — никто ничего не опрашивает; как только в таблице
   `transactions` появляется строка, событие «выталкивается» клиенту.

Второй путь и делает **Debezium** — это **CDC** (Change Data Capture): он читает
журнал изменений PostgreSQL (WAL) и публикует каждое изменение строки в Kafka.
Наше приложение **не вызывает** Debezium — он наблюдает за БД со стороны.

```
account-service ──INSERT──▶ Postgres (bankdb.public.transactions)
                                  │
                                  │  WAL (логическая репликация, плагин pgoutput)
                                  ▼
                          Debezium (Kafka Connect)
                                  │  topic: bankdemo.public.transactions
                                  ▼
                               Kafka
                                  │
                                  ▼
                     ws-service ──WebSocket──▶ браузер (лента операций)
```

Важно: account-service просто пишет транзакцию в БД в рамках своей бизнес-логики.
О том, что её надо «разослать», он не знает — это полностью забота Debezium + Kafka
+ ws-service. Так пишущий сервис развязан с теми, кто реагирует на изменения.

## Что требуется от PostgreSQL

Debezium для Postgres использует **логическую репликацию** через встроенный
плагин `pgoutput`. Для этого в `docker-compose.yml` Postgres запускается с
параметрами:

```yaml
command:
  - "postgres"
  - "-c"
  - "wal_level=logical"      # без этого логического декодирования не будет
  - "-c"
  - "max_wal_senders=10"
  - "-c"
  - "max_replication_slots=10"
```

Дополнительно Debezium при старте сам создаёт в Postgres:

- **слот репликации** `debezium_bank_slot` — закладка в WAL, с которой коннектор
  продолжает чтение после перезапуска (живёт в томе `pgdata`);
- **публикацию** `debezium_bank_pub` — набор таблиц, за которыми он следит
  (благодаря `publication.autocreate.mode=filtered` — только `public.transactions`).

Пользователь `postgres` — суперпользователь, у него есть право `REPLICATION` и
право создавать публикации, поэтому ничего вручную настраивать не нужно.

## Разбор конфига коннектора

`infra/debezium/account-connector.json` (регистрируется как «голый» config, см.
ниже). По полям:

| Параметр | Значение | Что делает |
|----------|----------|------------|
| `connector.class` | `…PostgresConnector` | Коннектор-источник для PostgreSQL. |
| `database.*` | `postgres:5432`, БД `bankdb` | Куда подключаться (имя сервиса в сети Docker). |
| `plugin.name` | `pgoutput` | Плагин логического декодирования (встроен в PG 10+). |
| `slot.name` / `publication.name` | `debezium_bank_slot` / `debezium_bank_pub` | Имена слота и публикации. |
| `publication.autocreate.mode` | `filtered` | Создать публикацию только для таблиц из `table.include.list`. |
| `schema.include.list` | `public` | Какую схему слушать. |
| `table.include.list` | `public.transactions` | **Слушаем только таблицу транзакций.** |
| `topic.prefix` | `bankdemo` | Префикс имён топиков. |
| `snapshot.mode` | `initial` | При первом запуске снять снимок существующих строк, затем стримить новые. |
| `decimal.handling.mode` | `string` | `numeric` (amount) отдавать строкой (`"500.00"`), а не в base64. |
| `time.precision.mode` | `connect` | Временные типы — в стандартных типах Kafka Connect. |
| `key.converter` / `value.converter` | `JsonConverter`, `schemas.enable=false` | Сообщения в виде «чистого» JSON без обёртки-схемы. |
| `transforms=unwrap` | `ExtractNewRecordState` | **Разворачивает** событие из конверта Debezium в плоскую строку (см. ниже). |
| `transforms.unwrap.delete.handling.mode` | `drop` + `drop.tombstones=true` | Удаления и tombstone-сообщения не публикуются (мы только вставляем транзакции). |

Имя топика собирается как `topic.prefix.schema.table` →
**`bankdemo.public.transactions`**. Именно его читает ws-service.

## Как выглядит событие

Допустим, account-service выполнил перевод и вставил строку:

```sql
INSERT INTO transactions(id, account_id, owner_username, type, amount, currency,
                         counterparty, description, status, created_at)
VALUES ('b2118a59-…', '…', 'alice', 'DEBIT', 500.00, 'RUB',
        '+79990000002', 'typed test', 'COMPLETED', now());
```

«Сырое» событие Debezium — это конверт с `before`/`after`/`op`/`source`:

```json
{
  "before": null,
  "after": {
    "id": "b2118a59-…", "account_id": "…", "owner_username": "alice",
    "type": "DEBIT", "amount": "500.00", "currency": "RUB",
    "counterparty": "+79990000002", "description": "typed test",
    "status": "COMPLETED", "created_at": "2026-06-08T15:17:09.957948Z"
  },
  "op": "c",
  "source": { "db": "bankdb", "schema": "public", "table": "transactions", "lsn": 123456 }
}
```

Трансформация **`ExtractNewRecordState` (unwrap)** оставляет только содержимое
`after` — на топик попадает плоская строка (её и потребляет ws-service):

```json
{
  "id": "b2118a59-…",
  "account_id": "…",
  "owner_username": "alice",
  "type": "DEBIT",
  "amount": "500.00",
  "currency": "RUB",
  "counterparty": "+79990000002",
  "description": "typed test",
  "status": "COMPLETED",
  "created_at": "2026-06-08T15:17:09.957948Z"
}
```

Ключ сообщения = первичный ключ строки: `{"id":"b2118a59-…"}`.

Обратите внимание: ключи — **snake_case** (имена колонок), а `amount` — **строка**
(из-за `decimal.handling.mode=string`). Поэтому в ws-service запись для разбора
описана так:

```java
public record TransactionChange(
    String id,
    @JsonProperty("owner_username") String ownerUsername,
    String type, String amount, String currency, String counterparty,
    String description, String status,
    @JsonProperty("created_at") String createdAt) {}
```

ws-service маршрутизирует событие нужному пользователю по `owner_username`
(это поле денормализовано в таблицу `transactions` специально для роутинга).

## Регистрация коннектора

Коннектор создаётся не вручную, а одноразовым контейнером `connector-init`
(см. `docker-compose.yml`): он ждёт REST Kafka Connect **и** готовность
`account-service` (чтобы таблица `transactions` уже существовала), затем делает
идемпотентный `PUT` конфигом:

```bash
PUT http://connect:8083/connectors/account-connector/config
Content-Type: application/json
<содержимое infra/debezium/account-connector.json>
```

Поэтому файл `account-connector.json` содержит **только объект config** (без
обёртки `{"name":…, "config":…}`) — это формат, который ждёт endpoint `/config`.

Зарегистрировать вручную (если правили конфиг):

```bash
docker compose up -d connector-init
# или напрямую:
curl -X PUT -H 'Content-Type: application/json' \
  --data @infra/debezium/account-connector.json \
  http://localhost:8085/connectors/account-connector/config
```

## Как посмотреть, что всё работает

```bash
# статус коннектора и задачи
curl -s http://localhost:8085/connectors/account-connector/status | jq

# поток изменений вживую (сделайте перевод/пополнение в соседнем окне)
docker compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic bankdemo.public.transactions --from-beginning
```

Или в **Kafka UI** (http://localhost:8090) → Topics → `bankdemo.public.transactions`,
а также раздел Kafka Connect → коннектор `debezium`.

## Эксплуатационные заметки и подводные камни

- **Таблица должна существовать до старта коннектора** — иначе
  `publication.autocreate` не сможет добавить её в публикацию. Поэтому
  `connector-init` ждёт, пока `account-service` станет `healthy` (а он к тому
  моменту уже создал схему через Hibernate).
- **Слот репликации живёт в Postgres.** Kafka в этом демо без тома, поэтому при
  пересоздании Kafka теряются служебные топики Connect, и `connector-init`
  регистрирует коннектор заново — он переиспользует тот же слот в Postgres.
  Полный сброс — `docker compose down -v` (удалит `pgdata` вместе со слотом).
- **`snapshot.mode=initial`**: при первой регистрации Debezium снимает снимок уже
  существующих строк `transactions` (это события с `op="r"`). ws-service читает
  с `auto-offset-reset=latest`, поэтому исторические события в ленту не сыплются —
  история подгружается отдельно по REST, а WebSocket показывает только новые.
- **Один таск.** У PostgreSQL-коннектора `tasks.max=1` (одна задача на БД) — это
  ограничение логической репликации, а не нашей конфигурации.
- **Не висит слот.** Если надолго остановить только Connect, слот в Postgres
  продолжит удерживать WAL — для демо неважно, но в проде за этим следят.
