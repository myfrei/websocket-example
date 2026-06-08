# account-background-service

Асинхронный воркер: читает команды переводов из Kafka и исполняет их через REST
account-service. Демонстрирует разнесение «приёма» и «обработки» операции.

- **Порт:** нет (`spring.main.web-application-type=none`)
- **Зависимости:** Kafka (консьюмер `transfer-commands`), account-service (REST)
- **Сборка:** `mvn -f pom.xml spring-boot:run`

## Поток

1. Подписан на топик `transfer-commands` (группа `account-background`).
2. На каждое сообщение вызывает `POST /internal/transfers/apply` account-service
   с заголовком `X-Internal-Api-Key`.

```
Kafka(transfer-commands) ──▶ account-background-service ──REST──▶ account-service
```

## Конфигурация Kafka (фабрика)

`KafkaConsumerConfig` определяет `ConsumerFactory<String, TransferCommand>` с
`JsonDeserializer` (типизированная десериализация, `ignoreTypeHeaders`,
`trustedPackages=*`), обёрнутый в `ErrorHandlingDeserializer` (защита от
«ядовитых» сообщений), и `ConcurrentKafkaListenerContainerFactory` с
`concurrency=3` (топик на 3 партиции — горизонтальное масштабирование).
Листенер получает сразу объект `TransferCommand`, без `String` и ручного парсинга:

```java
@KafkaListener(topics = "${app.topics.transfer-commands}")
public void onTransferCommand(TransferCommand command) { ... }
```

## Переменные окружения

`SPRING_KAFKA_BOOTSTRAP_SERVERS`, `APP_ACCOUNT_SERVICE_URL`, `APP_INTERNAL_API_KEY`.
