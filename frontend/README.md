# frontend

React + Vite + TypeScript. Кабинет банка с ролевым разделением (клиент/менеджер),
переводами, лентой операций и чатом в реальном времени. WebSocket — нативный,
с JSON-командами (без STOMP/SockJS).

- **Дев-сервер:** `npm install && npm run dev` → http://localhost:5173
- **Прод:** `npm run build` → статика в `dist/` (в Docker раздаётся nginx на `:3000`)
- **API:** общается с gateway (`http://localhost:8080`), WebSocket `ws://localhost:8080/ws`

## Экраны

- **Вход/регистрация.** Регистрация показывает «Ожидайте активации менеджером»
  (токен не выдаётся). Вход до активации — ошибка.
- **Кабинет клиента** (роль `USER`): баланс, перевод по телефону/счёту, лента
  операций, чат с менеджером, консоль WebSocket.
- **Кабинет менеджера** (роль `MANAGER`): активация заявок, открытие счетов,
  пополнение баланса, список чатов и ответы клиентам, консоль WebSocket.

## Консоль WebSocket

Панель показывает отправленные (`▲ SEND`) и полученные (`▼ RECV`) кадры — то же,
что видно в Postman. Можно отправлять команды кнопками или ввести JSON вручную
(например `{"command":"getBalance"}`).

## Ключевые модули

| Файл | Назначение |
|------|------------|
| `src/hooks/useBankSocket.ts` | Нативный WebSocket: `send(command)`, маршрутизация событий, журнал кадров. |
| `src/auth/AuthContext.tsx` | JWT + роль в `localStorage`. |
| `src/pages/UserDashboard.tsx` | Кабинет клиента. |
| `src/pages/ManagerDashboard.tsx` | Кабинет менеджера. |
| `src/components/*` | `BalanceCard`, `TransferForm`, `ActivityFeed`, `ChatPanel`, `WsLog`, `TopBar`. |

## Переменные сборки (опционально)

`VITE_API_BASE` (по умолчанию `http://localhost:8080`),
`VITE_WS_URL` (по умолчанию `ws://localhost:8080/ws`).
