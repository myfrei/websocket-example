import { useCallback, useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { useAuth } from '../auth/AuthContext';
import { apiFetch, ApiError } from '../api/client';
import type { ChatMessage, ChatSummary, PendingUser } from '../types';
import { useBankSocket } from '../hooks/useBankSocket';
import type { WsEvent } from '../hooks/useBankSocket';
import { ChatPanel } from '../components/ChatPanel';
import { WsLog } from '../components/WsLog';
import { TopBar } from '../components/TopBar';

const QUICK = ['getChats', 'ping'];

function PendingUsersCard({ token }: { token: string | null }) {
  const [pending, setPending] = useState<PendingUser[]>([]);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(async () => {
    try {
      setPending(await apiFetch<PendingUser[]>('/api/manager/users/pending', { token }));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Ошибка загрузки');
    }
  }, [token]);

  useEffect(() => {
    void reload();
  }, [reload]);

  const approve = async (username: string) => {
    await apiFetch(`/api/manager/users/${username}/approve`, { method: 'POST', token });
    await reload();
  };

  return (
    <section className="card">
      <div className="feed__head">
        <h2>Заявки на регистрацию</h2>
        <button type="button" className="btn-ghost btn-sm" onClick={() => void reload()}>
          Обновить
        </button>
      </div>
      {error && <div className="note note--err">{error}</div>}
      {pending.length === 0 ? (
        <p className="feed__empty">Нет заявок, ожидающих активации.</p>
      ) : (
        <ul className="plain-list">
          {pending.map((u) => (
            <li key={u.username} className="plain-list__item">
              <div>
                <strong>{u.fullName}</strong>
                <div className="muted">@{u.username} · {u.phone}</div>
              </div>
              <button type="button" className="btn-primary btn-sm" onClick={() => void approve(u.username)}>
                Активировать
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

function CreateAccountCard({ token }: { token: string | null }) {
  const [username, setUsername] = useState('');
  const [currency, setCurrency] = useState('RUB');
  const [initialBalance, setInitialBalance] = useState('0');
  const [note, setNote] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setNote(null);
    setError(null);
    try {
      await apiFetch('/api/manager/accounts', {
        method: 'POST',
        token,
        body: { username: username.trim(), currency, initialBalance: Number(initialBalance) },
      });
      setNote(`Счёт для ${username} создан`);
      setUsername('');
      setInitialBalance('0');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  };

  return (
    <section className="card">
      <h2>Открыть счёт</h2>
      <form className="stack" onSubmit={submit}>
        <label>
          Логин клиента
          <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="alice" required />
        </label>
        <div className="grid-2">
          <label>
            Валюта
            <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
              {['RUB', 'USD', 'EUR'].map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </label>
          <label>
            Начальный баланс
            <input type="number" min="0" step="0.01" value={initialBalance} onChange={(e) => setInitialBalance(e.target.value)} />
          </label>
        </div>
        {note && <div className="note note--ok">{note}</div>}
        {error && <div className="note note--err">{error}</div>}
        <button type="submit" className="btn-primary">Создать счёт</button>
      </form>
    </section>
  );
}

function TopUpCard({ token }: { token: string | null }) {
  const [username, setUsername] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [note, setNote] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setNote(null);
    setError(null);
    try {
      await apiFetch('/api/manager/accounts/topup', {
        method: 'POST',
        token,
        body: { username: username.trim(), amount: Number(amount), description: description.trim() || null },
      });
      setNote(`Баланс ${username} пополнен`);
      setAmount('');
      setDescription('');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  };

  return (
    <section className="card">
      <h2>Пополнить баланс</h2>
      <form className="stack" onSubmit={submit}>
        <label>
          Логин клиента
          <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="alice" required />
        </label>
        <label>
          Сумма
          <input type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required />
        </label>
        <label>
          Комментарий
          <input value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Бонус" />
        </label>
        {note && <div className="note note--ok">{note}</div>}
        {error && <div className="note note--err">{error}</div>}
        <button type="submit" className="btn-primary">Пополнить</button>
      </form>
    </section>
  );
}

export function ManagerDashboard() {
  const { token, user, logout } = useAuth();
  const [chats, setChats] = useState<ChatSummary[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const selectedRef = useRef<string | null>(null);
  const sendRef = useRef<(payload: unknown) => void>(() => {});

  const handleEvent = useCallback((msg: WsEvent) => {
    switch (msg.event) {
      case 'chats':
        setChats(msg.data as ChatSummary[]);
        break;
      case 'messages':
        setMessages(msg.data as ChatMessage[]);
        break;
      case 'chatMessage': {
        const m = msg.data as ChatMessage;
        if (m.chatUser === selectedRef.current) {
          setMessages((prev) => [...prev, m]);
        }
        sendRef.current({ command: 'getChats' });
        break;
      }
      case 'chatOpened':
      case 'chatClosed':
        sendRef.current({ command: 'getChats' });
        break;
      default:
        break;
    }
  }, []);

  const { status, send, log, clearLog } = useBankSocket(token, handleEvent);

  useEffect(() => {
    sendRef.current = send;
  }, [send]);

  useEffect(() => {
    if (status === 'connected') {
      send({ command: 'getChats' });
    }
  }, [status, send]);

  const openChat = (chatUser: string) => {
    setSelected(chatUser);
    selectedRef.current = chatUser;
    setMessages([]);
    send({ command: 'getMessages', chatUser });
  };

  return (
    <div className="dashboard">
      <TopBar status={status} fullName={user?.fullName} username={user?.username} role="Менеджер" onLogout={logout} />

      <main className="dashboard__grid">
        <div className="dashboard__col">
          <PendingUsersCard token={token} />
          <CreateAccountCard token={token} />
          <TopUpCard token={token} />
        </div>

        <div className="dashboard__col">
          <section className="card">
            <div className="feed__head">
              <h2>Чаты клиентов</h2>
              <button type="button" className="btn-ghost btn-sm" onClick={() => send({ command: 'getChats' })}>
                Обновить
              </button>
            </div>
            {chats.length === 0 ? (
              <p className="feed__empty">Чатов пока нет.</p>
            ) : (
              <ul className="plain-list">
                {chats.map((c) => (
                  <li key={c.chatId} className="plain-list__item">
                    <div>
                      <strong>{c.user}</strong>
                      <div className="muted">{c.status}</div>
                    </div>
                    <button
                      type="button"
                      className={selected === c.user ? 'btn-primary btn-sm' : 'btn-ghost btn-sm'}
                      onClick={() => openChat(c.user)}
                    >
                      Открыть
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <ChatPanel
            title={selected ? `Чат с ${selected}` : 'Чат с клиентом'}
            subtitle={selected ? undefined : 'Выберите чат слева'}
            messages={messages}
            selfUsername={user?.username ?? ''}
            disabled={!selected}
            onSend={(text) => selected && send({ command: 'sendMessage', chatUser: selected, text })}
          />

          <WsLog status={status} log={log} quickCommands={QUICK} onSend={send} onClear={clearLog} />
        </div>
      </main>
    </div>
  );
}
