import { useState } from 'react';
import type { FormEvent } from 'react';
import type { WsLogEntry, WsStatus } from '../hooks/useBankSocket';

const STATUS_LABEL: Record<WsStatus, string> = {
  connecting: 'подключение…',
  connected: 'онлайн',
  disconnected: 'оффлайн',
};

interface Props {
  status: WsStatus;
  log: WsLogEntry[];
  quickCommands: string[];
  onSend: (payload: unknown) => void;
  onClear: () => void;
}

export function WsLog({ status, log, quickCommands, onSend, onClear }: Props) {
  const [text, setText] = useState('{"command":"getBalance"}');

  const submit = (event: FormEvent) => {
    event.preventDefault();
    const trimmed = text.trim();
    if (!trimmed) {
      return;
    }
    let payload: unknown = { command: trimmed };
    if (trimmed.startsWith('{')) {
      try {
        payload = JSON.parse(trimmed);
      } catch {
        payload = null;
      }
    }
    if (payload) {
      onSend(payload);
    }
  };

  return (
    <section className="card wslog">
      <div className="wslog__head">
        <h2>WebSocket консоль</h2>
        <div className="wslog__status">
          <span className={`ws-dot ws-dot--${status}`} />
          {STATUS_LABEL[status]}
          <button type="button" className="btn-ghost btn-sm" onClick={onClear}>
            Очистить
          </button>
        </div>
      </div>

      <div className="wslog__quick">
        {quickCommands.map((cmd) => (
          <button key={cmd} type="button" onClick={() => onSend({ command: cmd })}>
            {cmd}
          </button>
        ))}
      </div>

      <form className="wslog__form" onSubmit={submit}>
        <input value={text} onChange={(e) => setText(e.target.value)} />
        <button type="submit" className="btn-primary btn-sm">
          Send
        </button>
      </form>

      <div className="wslog__list">
        {log.length === 0 ? (
          <p className="feed__empty">Кадры появятся здесь.</p>
        ) : (
          log.map((entry) => (
            <div key={entry.id} className={`wslog__line wslog__line--${entry.dir}`}>
              <span className="wslog__dir">{entry.dir === 'out' ? '▲ SEND' : '▼ RECV'}</span>
              <span className="wslog__ts">{entry.ts}</span>
              <code>{entry.text}</code>
            </div>
          ))
        )}
      </div>
    </section>
  );
}
