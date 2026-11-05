import { useState } from 'react';
import clsx from 'clsx';
import toast from 'react-hot-toast';
import { Plus, Trash2, Check, X, MessageSquare } from 'lucide-react';
import { useConversationStore } from '../store/conversationStore';

const ago = (iso) => {
  const m = Math.floor((Date.now() - new Date(iso)) / 60000);
  if (m < 1) return 'just now';
  if (m < 60) return `${m}m ago`;
  if (m < 1440) return `${Math.floor(m / 60)}h ago`;
  return `${Math.floor(m / 1440)}d ago`;
};

export default function ChatsPanel() {
  const { chats, activeId, newChat, selectChat, deleteChat } = useConversationStore();
  const [confirmId, setConfirmId] = useState(null);

  const del = async (id) => {
    await deleteChat(id);
    setConfirmId(null);
    toast.success('Chat deleted');
  };

  return (
    <div className="flex min-h-0 flex-1 flex-col p-3 gap-3">
      <button onClick={newChat}
        className="flex items-center justify-center gap-2 rounded-xl bg-indigo-600 py-2 text-sm font-medium text-white hover:bg-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-400">
        <Plus className="h-4 w-4" /> New chat
      </button>
      <ul className="min-h-0 flex-1 space-y-1 overflow-y-auto">
        {!chats.length && <li className="py-8 text-center text-sm text-slate-400">No chats yet. Ask your first question.</li>}
        {chats.map((c) => (
          <li key={c.id} className={clsx('flex items-center gap-1 rounded-lg pr-1', activeId === c.id ? 'bg-indigo-600/20' : 'hover:bg-slate-800')}>
            <button onClick={() => selectChat(c.id)} className="flex min-w-0 flex-1 items-start gap-2 p-2 text-left focus:outline-none focus:ring-2 focus:ring-indigo-500 rounded-lg">
              <MessageSquare className="mt-0.5 h-4 w-4 shrink-0 text-slate-400" />
              <span className="min-w-0">
                <span className="block truncate text-sm">{c.title}</span>
                <span className="block text-xs text-slate-400">{ago(c.updatedAt)}</span>
              </span>
            </button>
            {confirmId === c.id ? (
              <>
                <button aria-label="Confirm delete chat" onClick={() => del(c.id)} className="p-1 text-red-400"><Check className="h-4 w-4" /></button>
                <button aria-label="Cancel delete" onClick={() => setConfirmId(null)} className="p-1 text-slate-400"><X className="h-4 w-4" /></button>
              </>
            ) : (
              <button aria-label={`Delete chat ${c.title}`} onClick={() => setConfirmId(c.id)} className="p-1 text-slate-500 hover:text-red-400"><Trash2 className="h-4 w-4" /></button>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
