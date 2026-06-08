import { useState } from 'react';
import type { FormEvent } from 'react';
import type { ChatMessage } from '../types';

interface Props {
  title: string;
  subtitle?: string;
  messages: ChatMessage[];
  selfUsername: string;
  onSend: (text: string) => void;
  disabled?: boolean;
}

function formatTime(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
}

export function ChatPanel({ title, subtitle, messages, selfUsername, onSend, disabled }: Props) {
  const [text, setText] = useState('');

  const submit = (event: FormEvent) => {
    event.preventDefault();
    const trimmed = text.trim();
    if (!trimmed) {
      return;
    }
    onSend(trimmed);
    setText('');
  };

  return (
    <section className="card chat">
      <div className="chat__head">
        <h2>{title}</h2>
        {subtitle && <span className="chat__subtitle">{subtitle}</span>}
      </div>

      <div className="chat__messages">
        {messages.length === 0 ? (
          <p className="feed__empty">Сообщений пока нет.</p>
        ) : (
          messages.map((m, index) => {
            const mine = m.fromUsername === selfUsername;
            return (
              <div
                key={m.id ?? index}
                className={`chat__msg ${mine ? 'chat__msg--mine' : 'chat__msg--their'}`}
              >
                <div className="chat__bubble">
                  <div className="chat__meta">
                    {m.fromRole === 'MANAGER' ? 'Менеджер' : m.fromUsername} · {formatTime(m.createdAt)}
                  </div>
                  <div className="chat__text">{m.text}</div>
                </div>
              </div>
            );
          })
        )}
      </div>

      <form className="chat__form" onSubmit={submit}>
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder={disabled ? 'Выберите чат…' : 'Сообщение…'}
          disabled={disabled}
        />
        <button type="submit" className="btn-primary" disabled={disabled}>
          Отправить
        </button>
      </form>
    </section>
  );
}
