import { Sun, Moon } from 'lucide-react';
import { useThemeStore } from '../store/themeStore';

export default function ThemeToggle({ className = '' }) {
  const { theme, toggle } = useThemeStore();
  const isDark = theme === 'dark';
  const label = isDark ? 'Switch to light theme' : 'Switch to dark theme';
  return (
    <button type="button" onClick={toggle} aria-label={label} title={label}
      className={`rounded-lg border border-slate-700 p-2 text-slate-300 hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500 ${className}`}>
      {isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
    </button>
  );
}