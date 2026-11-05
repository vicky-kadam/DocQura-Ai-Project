import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Loader2 } from 'lucide-react';
import AuthShell, { Field, inputCls } from '../components/AuthShell.jsx';
import { useAuthStore } from '../store/authStore';
import { errMsg } from '../services/api';

export default function Login() {
  const { login, isLoading } = useAuthStore();
  const nav = useNavigate();
  const [f, setF] = useState({ username: '', password: '' });

  const submit = async (e) => {
    e.preventDefault();
    if (!f.username || !f.password) return toast.error('Enter your username and password');
    try {
      await login(f.username, f.password);
      nav('/', { replace: true });
    } catch (err) {
      toast.error(errMsg(err, 'Login failed'));
    }
  };

  return (
    <AuthShell title="Welcome back" subtitle="Sign in to chat with your documents">
      <form onSubmit={submit} className="space-y-4">
        <Field label="Username">
          <input className={inputCls} autoComplete="username" value={f.username}
            onChange={(e) => setF({ ...f, username: e.target.value })} />
        </Field>
        <Field label="Password">
          <input className={inputCls} type="password" autoComplete="current-password" value={f.password}
            onChange={(e) => setF({ ...f, password: e.target.value })} />
        </Field>
        <button disabled={isLoading}
          className="w-full flex items-center justify-center gap-2 rounded-lg bg-indigo-600 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-indigo-400">
          {isLoading && <Loader2 className="h-4 w-4 animate-spin" />} Sign in
        </button>
      </form>
      <p className="mt-4 text-center text-sm text-slate-400">
        New here? <Link to="/register" className="text-indigo-400 hover:underline">Create an account</Link>
      </p>
    </AuthShell>
  );
}
