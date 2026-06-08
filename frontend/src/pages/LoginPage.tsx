import { useState } from 'react';
import type { FormEvent } from 'react';
import { useAuth } from '../auth/AuthContext';
import { ApiError } from '../api/client';

type Mode = 'login' | 'register';

export function LoginPage() {
  const { login, register } = useAuth();
  const [mode, setMode] = useState<Mode>('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setInfo(null);
    setBusy(true);
    try {
      if (mode === 'login') {
        await login(username, password);
      } else {
        const response = await register({ username, password, fullName, phone });
        setInfo(response.message);
        setMode('login');
        setPassword('');
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Не удалось выполнить запрос');
    } finally {
      setBusy(false);
    }
  };

  const quickFill = (user: string, pass: string) => {
    setMode('login');
    setUsername(user);
    setPassword(pass);
    setError(null);
    setInfo(null);
  };

  return (
    <div className="auth-screen">
      <div className="auth-card">
        <div className="auth-brand">
          <span className="auth-logo">◎</span>
          <h1>WS Bank</h1>
          <p>Кабинет банка с лентой операций и чатом в реальном времени</p>
        </div>

        <div className="auth-tabs">
          <button type="button" className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>
            Вход
          </button>
          <button
            type="button"
            className={mode === 'register' ? 'active' : ''}
            onClick={() => setMode('register')}
          >
            Регистрация
          </button>
        </div>

        <form className="stack" onSubmit={submit}>
          <label>
            Логин
            <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" required />
          </label>

          {mode === 'register' && (
            <>
              <label>
                ФИО
                <input value={fullName} onChange={(e) => setFullName(e.target.value)} required />
              </label>
              <label>
                Телефон
                <input value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="+79990000003" required />
              </label>
            </>
          )}

          <label>
            Пароль
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              required
            />
          </label>

          {info && <div className="note note--ok">{info}</div>}
          {error && <div className="note note--err">{error}</div>}

          <button type="submit" className="btn-primary" disabled={busy}>
            {busy ? 'Подождите…' : mode === 'login' ? 'Войти' : 'Зарегистрироваться'}
          </button>
        </form>

        <div className="auth-demo">
          <span>Демо-входы:</span>
          <button type="button" onClick={() => quickFill('man', 'man123')}>
            man / man123 (менеджер)
          </button>
          <button type="button" onClick={() => quickFill('alice', 'alice123')}>
            alice / alice123
          </button>
          <button type="button" onClick={() => quickFill('bob', 'bob123')}>
            bob / bob123
          </button>
        </div>
      </div>
    </div>
  );
}
