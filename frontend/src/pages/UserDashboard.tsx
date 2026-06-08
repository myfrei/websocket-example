import { useCallback, useEffect, useRef, useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import type { Account, ChatMessage, Transaction } from '../types';
import { useBankSocket } from '../hooks/useBankSocket';
import type { WsEvent } from '../hooks/useBankSocket';
import { BalanceCard } from '../components/BalanceCard';
import { TransferForm } from '../components/TransferForm';
import { ActivityFeed } from '../components/ActivityFeed';
import { ChatPanel } from '../components/ChatPanel';
import { WsLog } from '../components/WsLog';
import { TopBar } from '../components/TopBar';

const QUICK = ['getBalance', 'getTransactions', 'getMessages', 'createChat', 'closeChat', 'ping'];

function normalizeTx(raw: Record<string, unknown>): Transaction {
  return {
    id: String(raw.id),
    type: raw.type === 'CREDIT' ? 'CREDIT' : 'DEBIT',
    amount: Number(raw.amount),
    currency: String(raw.currency),
    counterparty: String(raw.counterparty ?? ''),
    description: (raw.description as string | null) ?? null,
    status: raw.status === 'FAILED' ? 'FAILED' : 'COMPLETED',
    createdAt: typeof raw.createdAt === 'string' ? raw.createdAt : new Date().toISOString(),
  };
}

export function UserDashboard() {
  const { token, user, logout } = useAuth();
  const [account, setAccount] = useState<Account | null>(null);
  const [noAccount, setNoAccount] = useState(false);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const sendRef = useRef<(payload: unknown) => void>(() => {});

  const handleEvent = useCallback((msg: WsEvent) => {
    switch (msg.event) {
      case 'balance':
        setAccount(msg.data as Account);
        setNoAccount(false);
        break;
      case 'noAccount':
        setAccount(null);
        setNoAccount(true);
        break;
      case 'transactions':
        setTransactions((msg.data as Record<string, unknown>[]).map(normalizeTx));
        break;
      case 'transaction': {
        const tx = normalizeTx(msg.data as Record<string, unknown>);
        setTransactions((prev) => [tx, ...prev.filter((t) => t.id !== tx.id)].slice(0, 100));
        sendRef.current({ command: 'getBalance' });
        break;
      }
      case 'messages':
        setMessages(msg.data as ChatMessage[]);
        break;
      case 'chatMessage':
        setMessages((prev) => [...prev, msg.data as ChatMessage]);
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
      send({ command: 'getBalance' });
      send({ command: 'getTransactions' });
      send({ command: 'getMessages' });
    }
  }, [status, send]);

  return (
    <div className="dashboard">
      <TopBar status={status} fullName={user?.fullName} username={user?.username} role="Клиент" onLogout={logout} />

      <main className="dashboard__grid">
        <div className="dashboard__col">
          {account ? (
            <>
              <BalanceCard account={account} />
              <TransferForm token={token} defaultCurrency={account.currency} />
            </>
          ) : (
            <section className="card provisioning">
              {noAccount ? (
                <p>Счёт ещё не открыт. Обратитесь к менеджеру для открытия счёта.</p>
              ) : (
                <>
                  <div className="spinner" />
                  <p>Загружаем данные…</p>
                </>
              )}
            </section>
          )}
          <ChatPanel
            title="Чат с менеджером"
            messages={messages}
            selfUsername={user?.username ?? ''}
            onSend={(text) => send({ command: 'sendMessage', text })}
          />
        </div>

        <div className="dashboard__col">
          <ActivityFeed transactions={transactions} />
          <WsLog status={status} log={log} quickCommands={QUICK} onSend={send} onClear={clearLog} />
        </div>
      </main>
    </div>
  );
}
