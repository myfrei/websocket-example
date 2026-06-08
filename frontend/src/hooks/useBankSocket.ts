import { useCallback, useEffect, useRef, useState } from 'react';
import { WS_URL } from '../config';

export type WsStatus = 'connecting' | 'connected' | 'disconnected';

export interface WsLogEntry {
  id: number;
  dir: 'out' | 'in';
  text: string;
  ts: string;
}

export interface WsEvent {
  event: string;
  data?: unknown;
  message?: string;
}

export function useBankSocket(token: string | null, onEvent: (event: WsEvent) => void) {
  const [status, setStatus] = useState<WsStatus>('disconnected');
  const [log, setLog] = useState<WsLogEntry[]>([]);
  const wsRef = useRef<WebSocket | null>(null);
  const onEventRef = useRef(onEvent);
  onEventRef.current = onEvent;
  const counterRef = useRef(0);

  const appendLog = useCallback((dir: 'out' | 'in', text: string) => {
    const entry: WsLogEntry = {
      id: counterRef.current++,
      dir,
      text,
      ts: new Date().toLocaleTimeString('ru-RU'),
    };
    setLog((prev) => [entry, ...prev].slice(0, 200));
  }, []);

  const send = useCallback(
    (payload: unknown) => {
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        const text = JSON.stringify(payload);
        ws.send(text);
        appendLog('out', text);
      }
    },
    [appendLog],
  );

  const clearLog = useCallback(() => setLog([]), []);

  useEffect(() => {
    if (!token) {
      return;
    }
    let closedByUs = false;
    let reconnectTimer: ReturnType<typeof setTimeout> | undefined;

    const connect = () => {
      const ws = new WebSocket(`${WS_URL}?token=${encodeURIComponent(token)}`);
      wsRef.current = ws;
      setStatus('connecting');
      ws.onopen = () => setStatus('connected');
      ws.onmessage = (event) => {
        const raw = typeof event.data === 'string' ? event.data : String(event.data);
        appendLog('in', raw);
        let parsed: WsEvent | null = null;
        try {
          parsed = JSON.parse(raw) as WsEvent;
        } catch {
          parsed = null;
        }
        if (parsed) {
          onEventRef.current(parsed);
        }
      };
      ws.onclose = () => {
        setStatus('disconnected');
        if (!closedByUs) {
          reconnectTimer = setTimeout(connect, 3000);
        }
      };
      ws.onerror = () => ws.close();
    };

    connect();

    return () => {
      closedByUs = true;
      if (reconnectTimer) {
        clearTimeout(reconnectTimer);
      }
      wsRef.current?.close();
    };
  }, [token, appendLog]);

  return { status, send, log, clearLog };
}
