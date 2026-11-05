import api from './api';
import { useAuthStore } from '../store/authStore';

export const chatApi = {
  query: (body) => api.post('/chat/query', body).then((r) => r.data.data),
  similarity: (body) => api.post('/chat/search/similarity', body).then((r) => r.data.data),

  // Raw fetch: chunked plain-text body padhne ke liye
  async stream(body, onToken) {
    const token = useAuthStore.getState().token;
    const res = await fetch('/api/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...(token && { Authorization: `Bearer ${token}` }) },
      body: JSON.stringify(body),
    });
    if (res.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/login';
      throw new Error('Session expired');
    }
    if (!res.ok || !res.body) throw new Error(`Stream failed (${res.status})`);
    const reader = res.body.getReader();
    const decoder = new TextDecoder();
    for (;;) {
      const { done, value } = await reader.read();
      if (done) break;
      onToken(decoder.decode(value, { stream: true }));
    }
  },
};