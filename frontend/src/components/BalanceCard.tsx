import type { Account } from '../types';

function formatMoney(amount: number, currency: string): string {
  try {
    return new Intl.NumberFormat('ru-RU', { style: 'currency', currency }).format(amount);
  } catch {
    return `${amount.toFixed(2)} ${currency}`;
  }
}

export function BalanceCard({ account }: { account: Account }) {
  return (
    <section className="balance-card">
      <div className="balance-card__row">
        <span className="balance-card__label">Текущий баланс</span>
        <span className="balance-card__chip">{account.currency}</span>
      </div>
      <div className="balance-card__amount">{formatMoney(account.balance, account.currency)}</div>
      <div className="balance-card__meta">
        <div>
          <span className="balance-card__caption">Владелец</span>
          <strong>{account.fullName}</strong>
        </div>
        <div>
          <span className="balance-card__caption">Счёт</span>
          <strong>{account.accountNumber}</strong>
        </div>
        <div>
          <span className="balance-card__caption">Телефон</span>
          <strong>{account.phone}</strong>
        </div>
      </div>
    </section>
  );
}
