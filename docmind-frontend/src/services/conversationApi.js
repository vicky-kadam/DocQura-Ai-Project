// TODO(backend): replace with real endpoint
// Backed by localStorage, namespaced per logged-in user.
import { useAuthStore } from '../store/authStore';

const uid = () => useAuthStore.getState().user?.id ?? 'anon';
const kChats = () => `docmind:${uid()}:conversations`;
const kMsgs = (id) => `docmind:${uid()}:messages:${id}`;
const read = (k, d) => { try { return JSON.parse(localStorage.getItem(k)) ?? d; } catch { return d; } };

export const conversationApi = {
  async list() {
    return read(kChats(), []).sort((a, b) => b.updatedAt.localeCompare(a.updatedAt));
  },
  async getMessages(id) {
    return read(kMsgs(id), []);
  },
  async remove(id) {
    localStorage.removeItem(kMsgs(id));
    localStorage.setItem(kChats(), JSON.stringify(read(kChats(), []).filter((c) => c.id !== id)));
  },
  // local-cache helper (goes away when real endpoints exist)
  save(chats, id, messages) {
    localStorage.setItem(kChats(), JSON.stringify(chats));
    if (id) localStorage.setItem(kMsgs(id), JSON.stringify(messages));
  },
  getPref: (key, d) => read(`docmind:${uid()}:pref:${key}`, d),
  setPref: (key, v) => localStorage.setItem(`docmind:${uid()}:pref:${key}`, JSON.stringify(v)),
};
