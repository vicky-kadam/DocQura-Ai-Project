import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '../services/authApi';

export const useAuthStore = create(
  persist(
    (set) => ({
      user: null,
      token: null,
      isLoading: false,
      login: async (username, password) => {
        set({ isLoading: true });
        try {
          const { accessToken, user } = await authApi.login(username, password);
          set({ token: accessToken, user });
        } finally {
          set({ isLoading: false });
        }
      },
      register: async (username, email, password) => {
        set({ isLoading: true });
        try {
          await authApi.register(username, email, password);
        } finally {
          set({ isLoading: false });
        }
      },
      logout: () => {
        Object.keys(localStorage)
          .filter((k) => k.startsWith('docmind:'))
          .forEach((k) => localStorage.removeItem(k));
        set({ user: null, token: null });
      },
    }),
    { name: 'docmind-auth', partialize: (s) => ({ user: s.user, token: s.token }) }
  )
);
