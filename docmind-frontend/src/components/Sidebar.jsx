import { useRef, useState } from 'react';
import clsx from 'clsx';
import toast from 'react-hot-toast';
import { FileText, UploadCloud, Trash2, Check, X, Loader2, Layers } from 'lucide-react';
import { useDocumentStore } from '../store/documentStore';
import { errMsg } from '../services/api';
import ChatsPanel from './ChatsPanel.jsx';

export default function Sidebar() {
  const [tab, setTab] = useState('chats');
  return (
    <aside className="flex w-72 shrink-0 flex-col border-r border-slate-800 bg-slate-900/80">
      <div className="flex border-b border-slate-800 p-2 gap-1" role="tablist">
        {['chats', 'documents'].map((t) => (
          <button key={t} role="tab" aria-selected={tab === t} onClick={() => setTab(t)}
            className={clsx('flex-1 rounded-lg py-1.5 text-sm capitalize focus:outline-none focus:ring-2 focus:ring-indigo-500',
              tab === t ? 'bg-indigo-600/20 text-indigo-300' : 'text-slate-400 hover:bg-slate-800')}>
            {t}
          </button>
        ))}
      </div>
      {tab === 'chats' ? <ChatsPanel /> : <DocumentsPanel />}
    </aside>
  );
}

function DocumentsPanel() {
  const { documents, selectedId, loading, select, upload, remove } = useDocumentStore();
  const [uploading, setUploading] = useState(false);
  const [confirmId, setConfirmId] = useState(null);
  const input = useRef(null);

  const onFiles = async (files) => {
    if (!files.length) return;
    setUploading(true);
    try {
      await upload(files);
      toast.success('Upload complete');
    } catch (e) {
      toast.error(errMsg(e, 'Upload failed'));
    } finally {
      setUploading(false);
    }
  };
  const del = async (id) => {
    try { await remove(id); toast.success('Document deleted'); }
    catch (e) { toast.error(errMsg(e, 'Delete failed')); }
    setConfirmId(null);
  };

  return (
    <div className="flex min-h-0 flex-1 flex-col p-3 gap-3">
      <input ref={input} type="file" multiple hidden accept=".pdf,.doc,.docx,.md,.txt,.csv"
        onChange={(e) => { onFiles([...e.target.files]); e.target.value = ''; }} />
      <button onClick={() => input.current.click()}
        onDragOver={(e) => e.preventDefault()}
        onDrop={(e) => { e.preventDefault(); onFiles([...e.dataTransfer.files]); }}
        className="flex flex-col items-center gap-1 rounded-xl border-2 border-dashed border-slate-700 p-4 text-sm text-slate-300 hover:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500">
        {uploading ? <Loader2 className="h-6 w-6 animate-spin" /> : <UploadCloud className="h-6 w-6 text-indigo-400" />}
        <span><span className="text-indigo-400">Upload document</span> or drag here</span>
        <span className="text-xs text-slate-400">PDF, Word, Markdown, TXT, CSV (max 25MB)</span>
      </button>

      <button onClick={() => select(null)}
        className={clsx('flex items-center justify-between rounded-xl border px-3 py-2 text-left text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500',
          selectedId === null ? 'border-indigo-500 bg-indigo-600/10' : 'border-slate-800 hover:bg-slate-800')}>
        <span>All documents</span><span className="text-xs text-slate-400">{documents.length}</span>
      </button>

      <ul className="min-h-0 flex-1 space-y-2 overflow-y-auto">
        {loading && !documents.length && [1, 2, 3].map((i) => <li key={i} className="h-16 animate-pulse rounded-xl bg-slate-800" />)}
        {!loading && !documents.length && <li className="py-8 text-center text-sm text-slate-400">No documents yet. Upload one to get started.</li>}
        {documents.map((d) => (
          <li key={d.id} className={clsx('rounded-xl border p-3', selectedId === d.id ? 'border-indigo-500 bg-indigo-600/10' : 'border-slate-800')}>
            <div className="flex items-center gap-2">
              <button onClick={() => select(d.id)} className="flex min-w-0 flex-1 items-center gap-2 text-left focus:outline-none focus:ring-2 focus:ring-indigo-500 rounded">
                <FileText className="h-4 w-4 shrink-0 text-slate-400" />
                <span className="truncate text-sm">{d.fileName}</span>
              </button>
              {confirmId === d.id ? (
                <>
                  <button aria-label="Confirm delete" onClick={() => del(d.id)} className="p-1 text-red-400"><Check className="h-4 w-4" /></button>
                  <button aria-label="Cancel delete" onClick={() => setConfirmId(null)} className="p-1 text-slate-400"><X className="h-4 w-4" /></button>
                </>
              ) : (
                <button aria-label={`Delete ${d.fileName}`} onClick={() => setConfirmId(d.id)} className="p-1 text-slate-500 hover:text-red-400"><Trash2 className="h-4 w-4" /></button>
              )}
            </div>
            <div className="mt-1 flex items-center gap-1 text-xs text-slate-400"><Layers className="h-3 w-3" />{d.chunkCount ?? d.totalChunks ?? '-'} chunks</div>
          </li>
        ))}
      </ul>
    </div>
  );
}
