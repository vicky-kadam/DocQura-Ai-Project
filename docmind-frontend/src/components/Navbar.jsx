import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Menu, Search, LogOut, Sparkles } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { useDocumentStore } from '../store/documentStore';
import ThemeToggle from './ThemeToggle.jsx';

export default function Navbar({ onSearch }) {
  const { user, logout } = useAuthStore();
  const { documents, selectedId, toggleSidebar } = useDocumentStore();
  const nav = useNavigate();
  const [open, setOpen] = useState(false);
  const scope = documents.find((d) => d.id === selectedId)?.fileName ?? `All Documents (${documents.length})`;
  const initials = (user?.username || '?').slice(0, 2).toUpperCase();

  return (
    <header className="flex items-center justify-between gap-3 border-b border-slate-800 bg-slate-900 px-4 py-3">
      <div className="flex items-center gap-3 min-w-0">
        <button aria-label="Toggle sidebar" onClick={toggleSidebar}
          className="rounded-lg p-2 text-slate-300 hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500">
          <Menu className="h-5 w-5" />
        </button>
        <div className="h-9 w-9 shrink-0 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
          <Sparkles className="h-5 w-5 text-white" aria-hidden="true" />
        </div>
        <span className="text-lg font-semibold text-slate-100">DocMind</span>
        <span className="hidden md:inline-block truncate rounded-full border border-slate-700 px-3 py-1 text-xs text-slate-300">
          Scope: {scope}
        </span>
      </div>
      <div className="flex items-center gap-2">
        <ThemeToggle />
        <button onClick={onSearch}
          className="flex items-center gap-2 rounded-lg border border-slate-700 px-3 py-1.5 text-sm text-slate-300 hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500">
          <Search className="h-4 w-4" /> <span className="hidden sm:inline">Search chunks</span>
        </button>
        <div className="relative">
          <button aria-label="User menu" onClick={() => setOpen((o) => !o)}
            className="flex items-center gap-2 rounded-lg p-1 hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500">
            <span className="h-8 w-8 rounded-full bg-indigo-600 text-xs font-semibold text-white flex items-center justify-center">{initials}</span>
          </button>
          {open && (
            <div className="absolute right-0 z-30 mt-2 w-56 rounded-xl border border-slate-700 bg-slate-800 p-3 shadow-xl">
              <p className="text-sm font-medium text-slate-100">{user?.username}</p>
              <span className="mt-1 inline-block rounded bg-indigo-500/20 px-2 py-0.5 text-xs text-indigo-300">{user?.role}</span>
              <button onClick={() => { logout(); nav('/login', { replace: true }); }}
                className="mt-3 flex w-full items-center gap-2 rounded-lg px-2 py-2 text-sm text-red-300 hover:bg-slate-700">
                <LogOut className="h-4 w-4" /> Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}