import { create } from 'zustand';
import { documentApi } from '../services/documentApi';
import { useAuthStore } from './authStore';

const initial = { documents: [], selectedId: null, loading: false, sidebarOpen: true };

export const useDocumentStore = create((set, get) => ({
  ...initial,
  fetchDocuments: async () => {
    set({ loading: true });
    try {
      set({ documents: await documentApi.getMine() });
    } finally {
      set({ loading: false });
    }
  },
  select: (id) => set({ selectedId: id }),
  upload: async (files) => {
    await (files.length > 1 ? documentApi.uploadMultiple(files) : documentApi.upload(files[0]));
    await get().fetchDocuments();
  },
  remove: async (id) => {
    await documentApi.remove(id); // id only ever comes from the user's own list
    if (get().selectedId === id) set({ selectedId: null });
    await get().fetchDocuments();
  },
  toggleSidebar: () => set((s) => ({ sidebarOpen: !s.sidebarOpen })),
  reset: () => set(initial),
}));

useAuthStore.subscribe((s) => { if (!s.token) useDocumentStore.getState().reset(); });
