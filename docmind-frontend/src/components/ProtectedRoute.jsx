import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

// export function ProtectedRoute({ children }) {
//   const token = useAuthStore((s) => s.token);
//   return token ? children : <Navigate to="/login" replace />;
// }

// export function PublicRoute({ children }) {
//   const token = useAuthStore((s) => s.token);
//   return token ? <Navigate to="/" replace /> : children;
// }

// TEMP: auth off. Baad mein purana code wapas daalna.
export function ProtectedRoute({ children }) {
  return children;
}

export function PublicRoute({ children }) {
  return children;
}
