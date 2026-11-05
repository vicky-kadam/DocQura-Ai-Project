import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Loader2 } from 'lucide-react';
import AuthShell, { Field, inputCls } from '../components/AuthShell.jsx';
import { useAuthStore } from '../store/authStore';
import { errMsg } from '../services/api';

export default function Register() {
  const { register, isLoading } = useAuthStore();
  const nav = useNavigate();
  const [f, setF] = useState({ username: '', email: '', password: '', confirm: '' });
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    if (!f.username || !f.email || !f.password) return toast.error('Fill in all fields');
    if (f.password !== f.confirm) return toast.error('Passwords do not match');
    try {
      await register(f.username, f.email, f.password);
      toast.success('Account created. Sign in to continue.');
      nav('/login', { replace: true });
    } catch (err) {
      toast.error(errMsg(err, 'Registration failed'));
    }
  };

  return (
    <AuthShell title="Create your account" subtitle="Start asking questions about your documents">
      <form onSubmit={submit} className="space-y-4">
        <Field label="Username"><input className={inputCls} autoComplete="username" value={f.username} onChange={set('username')} /></Field>
        <Field label="Email"><input className={inputCls} type="email" autoComplete="email" value={f.email} onChange={set('email')} /></Field>
        <Field label="Password"><input className={inputCls} type="password" autoComplete="new-password" value={f.password} onChange={set('password')} /></Field>
        <Field label="Confirm password" error={f.confirm && f.confirm !== f.password ? 'Passwords do not match' : ''}>
          <input className={inputCls} type="password" autoComplete="new-password" value={f.confirm} onChange={set('confirm')} />
        </Field>
        <button disabled={isLoading}
          className="w-full flex items-center justify-center gap-2 rounded-lg bg-indigo-600 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-indigo-400">
          {isLoading && <Loader2 className="h-4 w-4 animate-spin" />} Create account
        </button>
      </form>
      <p className="mt-4 text-center text-sm text-slate-400">
        Already registered? <Link to="/login" className="text-indigo-400 hover:underline">Sign in</Link>
      </p>
    </AuthShell>
  );
}
