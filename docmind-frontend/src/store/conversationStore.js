import { create } from 'zustand';
import { conversationApi } from '../services/conversationApi';
import { useAuthStore } from './authStore';

const initial = { chats: [], activeId: null, messages: {}, streamMode: true };

export const useConversationStore = create((set, get) => ({
  ...initial,
  loadChats: async () => {
    const chats = await conversationApi.list();
    set({ chats, streamMode: conversationApi.getPref('streamMode', true) });
  },
  // id is minted client-side before the first message
  newChat: () => {
    const id = crypto.randomUUID();
    set((s) => ({ activeId: id, messages: { ...s.messages, [id]: [] } }));
    return id;
  },
  selectChat: async (id) => {
    if (!get().messages[id]) {
      const msgs = await conversationApi.getMessages(id);
      set((s) => ({ messages: { ...s.messages, [id]: msgs } }));
    }
    set({ activeId: id });
  },
  deleteChat: async (id) => {
    await conversationApi.remove(id);
    set((s) => {
      const { [id]: _drop, ...rest } = s.messages;
      return { chats: s.chats.filter((c) => c.id !== id), messages: rest, activeId: s.activeId === id ? null : s.activeId };
    });
  },
  appendMessage: (cid, msg) =>
    set((s) => {
      const msgs = [...(s.messages[cid] || []), msg];
      const now = new Date().toISOString();
      const ex = s.chats.find((c) => c.id === cid);
      const chat = ex
        ? { ...ex, updatedAt: now }
        : { id: cid, title: msg.content.slice(0, 40) || 'New chat', createdAt: now, updatedAt: now };
      const chats = [chat, ...s.chats.filter((c) => c.id !== cid)];
      conversationApi.save(chats, cid, msgs);
      return { messages: { ...s.messages, [cid]: msgs }, chats };
    }),
  patchMessage: (cid, mid, patch) =>
    set((s) => ({
      messages: { ...s.messages, [cid]: (s.messages[cid] || []).map((m) => (m.id === mid ? { ...m, ...patch } : m)) },
    })),
  flush: (cid) => conversationApi.save(get().chats, cid, get().messages[cid] || []),
  clearMessages: (cid) => {
    set((s) => ({ messages: { ...s.messages, [cid]: [] } }));
    get().flush(cid);
  },
  setStreamMode: (v) => {
    conversationApi.setPref('streamMode', v);
    set({ streamMode: v });
  },
  reset: () => set(initial),
}));

useAuthStore.subscribe((s) => { if (!s.token) useConversationStore.getState().reset(); });
