import { Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { ProtectedRoute, PublicRoute } from './components/ProtectedRoute.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Home from './pages/Home.jsx';

export default function App() {
  return (
    <>
      <Routes>
        <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
        <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
        <Route path="/*" element={<ProtectedRoute><Home /></ProtectedRoute>} />
      </Routes>
      <Toaster position="top-right" toastOptions={{ style: { background: 'var(--toast-bg)', color: 'var(--toast-fg)', border: '1px solid var(--scrollbar)' } }} />
    </>
  );
}