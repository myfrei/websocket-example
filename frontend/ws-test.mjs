import { Client } from '@stomp/stompjs';
import { readFileSync } from 'node:fs';

const token = readFileSync('/tmp/atok.txt', 'utf8').trim();
const GW = 'http://localhost:8080';

const client = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  connectHeaders: { Authorization: `Bearer ${token}` },
  reconnectDelay: 0,
  onConnect: () => {
    console.log('✅ STOMP connected through gateway');
    client.subscribe('/user/queue/activity', (msg) => {
      console.log('✅ WS MESSAGE RECEIVED:', msg.body);
      client.deactivate();
      process.exit(0);
    });
    console.log('subscribed to /user/queue/activity, triggering transfer...');
    setTimeout(async () => {
      const res = await fetch(`${GW}/api/transfers`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({
          toPhoneOrAccount: '+79990000002',
          amount: 250.5,
          currency: 'RUB',
          description: 'WS live test',
        }),
      });
      console.log('transfer POST status:', res.status);
    }, 600);
  },
  onStompError: (f) => {
    console.error('❌ STOMP error:', f.headers.message);
    process.exit(1);
  },
  onWebSocketError: (e) => {
    console.error('❌ WS error:', e?.message ?? e);
    process.exit(1);
  },
});

client.activate();
setTimeout(() => {
  console.error('❌ TIMEOUT: no WS message in 15s');
  process.exit(2);
}, 15000);
