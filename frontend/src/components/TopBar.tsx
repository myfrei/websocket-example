import type { WsStatus } from '../hooks/useBankSocket';

const WS_LABEL: Record<WsStatus, string> = {
  connecting: 'подключение…',
  connected: 'онлайн',
  disconnected: 'оффлайн',
};

interface Props {
  status: WsStatus;
  fullName?: string;
  username?: string;
  role: string;
  onLogout: () => void;
}

export function TopBar({ status, fullName, username, role, onLogout }: Props) {
  return (
    <header className="topbar">
      <div className="topbar__brand">
        <span className="auth-logo">◎</span>
        <strong>WS Bank</strong>
        <span className="topbar__role">{role}</span>
      </div>
      <div className="topbar__right">
        <span className={`ws-dot ws-dot--${status}`} title={`WebSocket: ${WS_LABEL[status]}`} />
        <span className="ws-label">{WS_LABEL[status]}</span>
        <div className="topbar__user">
          <span className="topbar__name">{fullName ?? username}</span>
          <span className="topbar__login">@{username}</span>
        </div>
        <button type="button" className="btn-ghost" onClick={onLogout}>
          Выйти
        </button>
      </div>
    </header>
  );
}
