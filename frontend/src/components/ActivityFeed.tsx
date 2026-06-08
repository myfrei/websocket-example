import type { Transaction } from '../types';

function formatAmount(tx: Transaction): string {
  const sign = tx.type === 'CREDIT' ? '+' : '−';
  try {
    const formatted = new Intl.NumberFormat('ru-RU', {
      style: 'currency',
      currency: tx.currency,
    }).format(tx.amount);
    return `${sign}${formatted}`;
  } catch {
    return `${sign}${tx.amount.toFixed(2)} ${tx.currency}`;
  }
}

function formatTime(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  return date.toLocaleString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function ActivityFeed({ transactions }: { transactions: Transaction[] }) {
  return (
    <section className="card feed">
      <div className="feed__head">
        <h2>Лента операций</h2>
        <span className="feed__live">● в реальном времени</span>
      </div>

      {transactions.length === 0 ? (
        <p className="feed__empty">Операций пока нет.</p>
      ) : (
        <ul className="feed__list">
          {transactions.map((tx) => (
            <li key={tx.id} className={`feed__item feed__item--${tx.type.toLowerCase()}`}>
              <div className={`feed__icon feed__icon--${tx.type.toLowerCase()}`}>
                {tx.type === 'CREDIT' ? '↓' : '↑'}
              </div>
              <div className="feed__body">
                <div className="feed__title">
                  {tx.description || (tx.type === 'CREDIT' ? 'Пополнение' : 'Перевод')}
                  {tx.status === 'FAILED' && <span className="feed__badge">Отклонено</span>}
                </div>
                <div className="feed__sub">
                  {tx.type === 'CREDIT' ? 'от ' : 'на '}
                  {tx.counterparty} · {formatTime(tx.createdAt)}
                </div>
              </div>
              <div
                className={`feed__amount feed__amount--${tx.type.toLowerCase()} ${
                  tx.status === 'FAILED' ? 'feed__amount--failed' : ''
                }`}
              >
                {formatAmount(tx)}
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
