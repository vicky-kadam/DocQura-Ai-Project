import { useEffect, useRef, useState } from 'react';
import clsx from 'clsx';
import toast from 'react-hot-toast';
import { Send, Sparkles, User, Bot, Wifi, WifiOff, Copy, Loader2 } from 'lucide-react';
import { useConversationStore } from '../store/conversationStore';
import { useDocumentStore } from '../store/documentStore';
import { chatApi } from '../services/chatApi';
import { errMsg } from '../services/api';
import { citationFile, citationPercent } from '../utils/citation';

const SUGGESTIONS = [
  'Summarize the key points and executive highlights of this document.',
  'What are the most critical questions and answers covered in this text?',
  'List all actionable takeaways, deadlines, and responsibilities mentioned.',
  'Explain the main technical concepts and architecture described.',
];

export default function ChatView() {
  const { activeId, messages, streamMode, setStreamMode, newChat, appendMessage, patchMessage, flush } = useConversationStore();
  const selectedId = useDocumentStore((s) => s.selectedId);
  const [input, setInput] = useState('');
  const [busy, setBusy] = useState(false);
  const endRef = useRef(null);
  const list = (activeId && messages[activeId]) || [];

  useEffect(() => { endRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [list]);

  const send = async (text) => {
    const q = (text ?? input).trim();
    if (!q || busy) return;
    const cid = activeId || newChat(); // id minted before the very first message
    const aid = crypto.randomUUID();
    const now = () => new Date().toISOString();
    setInput('');
    setBusy(true);
    appendMessage(cid, { id: crypto.randomUUID(), messageType: 'USER', content: q, createdAt: now() });
    appendMessage(cid, { id: aid, messageType: 'ASSISTANT', content: '', createdAt: now() });
    const body = { question: q, conversationId: cid, topK: 5, ...(selectedId && { documentId: selectedId }) };
    try {
      if (streamMode) {
        let acc = '';
        await chatApi.stream(body, (tok) => { acc += tok; patchMessage(cid, aid, { content: acc }); });
      } else {
        const r = await chatApi.query(body);
        patchMessage(cid, aid, { content: r.answer, citations: r.citations });
      }
    } catch (e) {
      patchMessage(cid, aid, { content: 'Could not get an answer. Please try again.' });
      toast.error(errMsg(e, 'Chat request failed'));
    } finally {
      flush(cid);
      setBusy(false);
    }
  };

  return (
    <section className="flex min-w-0 flex-1 flex-col bg-slate-950">
      <div className="flex items-center justify-between border-b border-slate-800 px-4 py-2 text-sm">
        <span className="text-slate-300">Context: {selectedId ? 'Selected document' : 'All uploaded documents'}</span>
        <button onClick={() => setStreamMode(!streamMode)} aria-pressed={streamMode}
          className={clsx('flex items-center gap-2 rounded-full border px-3 py-1 text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500',
            streamMode ? 'border-indigo-500 text-indigo-300' : 'border-slate-700 text-slate-300')}>
          {streamMode ? <Wifi className="h-3.5 w-3.5" /> : <WifiOff className="h-3.5 w-3.5" />}
          {streamMode ? 'Stream tokens' : 'Normal mode'}
        </button>
      </div>

      <div className="flex-1 overflow-y-auto px-4 py-6">
        {!list.length ? (
          <div className="mx-auto flex max-w-2xl flex-col items-center pt-10 text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-600">
              <Sparkles className="h-8 w-8 text-white" aria-hidden="true" />
            </div>
            <h2 className="mt-5 text-3xl font-bold text-slate-100">DocMind AI Document Assistant</h2>
            <p className="mt-2 text-slate-300">Upload reports, Word files or notes and chat with grounded facts and source citations.</p>
            <div className="mt-8 grid w-full gap-3 sm:grid-cols-2">
              {SUGGESTIONS.map((s) => (
                <button key={s} onClick={() => send(s)}
                  className="rounded-xl border border-slate-800 bg-slate-900 p-4 text-left text-sm text-slate-300 hover:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500">
                  {s}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="mx-auto max-w-3xl space-y-5">
            {list.map((m) => {
              const user = m.messageType === 'USER';
              return (
                <div key={m.id} className={clsx('flex gap-3', user && 'flex-row-reverse')}>
                  <div className={clsx('flex h-9 w-9 shrink-0 items-center justify-center rounded-xl', user ? 'bg-slate-800' : 'bg-indigo-600')}>
                    {user ? <User className="h-4 w-4" /> : <Bot className="h-4 w-4" />}
                  </div>
                  <div className={clsx('max-w-[85%] rounded-2xl px-4 py-3 text-sm whitespace-pre-wrap break-words', user ? 'bg-indigo-600 text-white' : 'border border-slate-800 bg-slate-900 text-slate-100')}>
                    {m.content || (busy && <Loader2 className="h-4 w-4 animate-spin" />)}
                    {!user && m.citations?.length > 0 && (
                      <div className="mt-3 space-y-1 border-t border-slate-800 pt-2 text-xs text-slate-400">
                        {m.citations.map((c, i) => (
                          <div key={i}>
                            {citationFile(c)}{c.pageNumber ? ` · p.${c.pageNumber}` : ''} · chunk {c.chunkIndex}
                            {citationPercent(c) !== null && ` · ${citationPercent(c)}% match`}
                          </div>
                        ))}
                      </div>
                    )}
                    {!user && m.content && (
                      <button aria-label="Copy answer" onClick={() => { navigator.clipboard.writeText(m.content); toast.success('Copied'); }}
                        className="mt-2 block text-slate-500 hover:text-slate-300"><Copy className="h-4 w-4" /></button>
                    )}
                  </div>
                </div>
              );
            })}
            <div ref={endRef} />
          </div>
        )}
      </div>

      <div className="border-t border-slate-800 bg-slate-900 p-4">
        <div className="mx-auto flex max-w-3xl items-end gap-2 rounded-2xl border border-slate-700 bg-slate-950 p-2">
          <textarea rows={1} value={input} onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); } }}
            placeholder={selectedId ? 'Ask a question about this document...' : 'Ask a question across all documents...'}
            className="max-h-40 flex-1 resize-none bg-transparent px-2 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none" />
          <button aria-label="Send message" onClick={() => send()} disabled={busy || !input.trim()}
            className="rounded-xl bg-indigo-600 p-2 text-white hover:bg-indigo-500 disabled:opacity-40 focus:outline-none focus:ring-2 focus:ring-indigo-400">
            <Send className="h-4 w-4" />
          </button>
        </div>
        <p className="mx-auto mt-2 max-w-3xl text-xs text-slate-400">Enter to send, Shift+Enter for newline</p>
      </div>
    </section>
  );
}