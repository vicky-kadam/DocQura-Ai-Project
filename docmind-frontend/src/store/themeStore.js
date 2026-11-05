import { create } from 'zustand';

const KEY = 'docqura-theme';

const systemTheme = () =>
  window.matchMedia?.('(prefers-color-scheme: light)').matches ? 'light' : 'dark';

const initialTheme = () => {
  try {
    const saved = localStorage.getItem(KEY);
    if (saved === 'light' || saved === 'dark') return saved;
  } catch { /* localStorage blocked */ }
  return systemTheme();
};

// <html data-theme="..."> set karta hai (CSS isi se palette badalta hai)
export const applyTheme = (theme) => {
  document.documentElement.dataset.theme = theme;
  document.querySelector('meta[name="theme-color"]')
    ?.setAttribute('content', theme === 'light' ? '#f1f5f9' : '#0f172a');
};

export const useThemeStore = create((set, get) => ({
  theme: initialTheme(),
  setTheme: (theme) => {
    try { localStorage.setItem(KEY, theme); } catch { /* ignore */ }
    applyTheme(theme);
    set({ theme });
  },
  toggle: () => get().setTheme(get().theme === 'dark' ? 'light' : 'dark'),
}));