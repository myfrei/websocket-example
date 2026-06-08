import { useAuth } from './auth/AuthContext';
import { LoginPage } from './pages/LoginPage';
import { UserDashboard } from './pages/UserDashboard';
import { ManagerDashboard } from './pages/ManagerDashboard';

export function App() {
  const { token, user } = useAuth();
  if (!token || !user) {
    return <LoginPage />;
  }
  return user.role === 'MANAGER' ? <ManagerDashboard /> : <UserDashboard />;
}
