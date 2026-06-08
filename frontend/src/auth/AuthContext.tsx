import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { apiFetch } from '../api/client';
import type { AuthResponse, AuthUser, RegisterForm, RegisterResponse } from '../types';

const STORAGE_KEY = 'bankdemo.auth';

interface StoredAuth {
  token: string;
  user: AuthUser;
}

interface AuthContextValue {
  token: string | null;
  user: AuthUser | null;
  login: (username: string, password: string) => Promise<void>;
  register: (form: RegisterForm) => Promise<RegisterResponse>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function loadStored(): StoredAuth | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as StoredAuth) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState<StoredAuth | null>(loadStored);

  const login = useCallback(async (username: string, password: string) => {
    const response = await apiFetch<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: { username, password },
    });
    const stored: StoredAuth = {
      token: response.token,
      user: {
        username: response.username,
        fullName: response.fullName,
        phone: response.phone,
        role: response.role,
      },
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
    setAuth(stored);
  }, []);

  const register = useCallback(async (form: RegisterForm) => {
    return apiFetch<RegisterResponse>('/api/auth/register', { method: 'POST', body: form });
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    setAuth(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ token: auth?.token ?? null, user: auth?.user ?? null, login, register, logout }),
    [auth, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
