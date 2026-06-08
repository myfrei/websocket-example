import { useState } from 'react';
import type { FormEvent } from 'react';
import { apiFetch, ApiError } from '../api/client';
import type { TransferAccepted } from '../types';

const CURRENCIES = ['RUB', 'USD', 'EUR'];

interface Props {
  token: string | null;
  defaultCurrency: string;
}

export function TransferForm({ token, defaultCurrency }: Props) {
  const [target, setTarget] = useState('');
  const [amount, setAmount] = useState('');
  const [currency, setCurrency] = useState(defaultCurrency);
  const [description, setDescription] = useState('');
  const [note, setNote] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setNote(null);
    setBusy(true);
    try {
      const result = await apiFetch<TransferAccepted>('/api/transfers', {
        method: 'POST',
        token,
        body: {
          toPhoneOrAccount: target.trim(),
          amount: Number(amount),
          currency,
          description: description.trim() || null,
        },
      });
      setNote(`Перевод принят в обработку (№ ${result.reference.slice(0, 8)}). Следите за лентой.`);
      setTarget('');
      setAmount('');
      setDescription('');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Не удалось отправить перевод');
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="card transfer">
      <h2>Новый перевод</h2>
      <p className="card__hint">По номеру телефона или номеру счёта</p>
      <form onSubmit={submit} className="stack">
        <label>
          Получатель
          <input
            value={target}
            onChange={(e) => setTarget(e.target.value)}
            placeholder="+79990000002 или ACC1000000002"
            required
          />
        </label>
        <div className="grid-2">
          <label>
            Сумма
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="1000.00"
              required
            />
          </label>
          <label>
            Валюта
            <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
              {CURRENCIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>
        </div>
        <label>
          Назначение
          <input
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="За кофе"
          />
        </label>

        {note && <div className="note note--ok">{note}</div>}
        {error && <div className="note note--err">{error}</div>}

        <button type="submit" className="btn-primary" disabled={busy}>
          {busy ? 'Отправка…' : 'Перевести'}
        </button>
      </form>
    </section>
  );
}
