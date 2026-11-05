import { useState } from 'react';
import clsx from 'clsx';
import toast from 'react-hot-toast';
import { Search, X, Loader2 } from 'lucide-react';
import { chatApi } from '../services/chatApi';
import { errMsg } from '../services/api';
import { useDocumentStore } from '../store/documentStore';
import { citationFile, citationPercent } from '../utils/citation';

const SNIPPET_LIMIT = 280;

// 70%+ = strong, 40%+ = medium, baaki weak
const tone = (p) =>
  p === null ? 'bg-slate-800 text-slate-300'
    : p >= 70 ? 'bg-emerald-500/15 text-emerald-400'
    : p >= 40 ? 'bg-amber-500/15 text-amber-400'
    : 'bg-slate-800 text-slate-300';

function MatchCard({ m, rank }) {
  const [open, setOpen] = useState(false);
  const pct = citationPercent(m);
  const text = m.snippet || '';
  const long = text.length > SNIPPET_LIMIT;
  const shown = open || !long ? text : `${text.slice(0, SNIPPET_LIMIT).trimEnd()}…`;

  return (
    <div className="rounded-xl border border-slate-800 p-3">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 text-xs text-slate-400">
          <span className="font-semibold text-slate-200">#{rank}</span>{' '}
          <span className="break-all">{citationFile(m)}</span>
          {m.chunkIndex != null && <> · chunk {m.chunkIndex}</>}
          {m.pageNumber ? <> · p.{m.pageNumber}</> : null}
        </div>
        <span className={clsx('shrink-0 rounded-full px-2 py-0.5 text-xs font-medium', tone(pct))}>
          {pct === null ? 'n/a' : `${pct}% match`}
        </span>
      </div>
      {pct !== null && (
        <div className="mt-2 h-1 overflow-hidden rounded-full bg-slate-800" aria-hidden="true">
          <div className="h-full rounded-full bg-indigo-500" style={{ width: `${pct}%` }} />
        </div>
      )}
      <p className="mt-2 whitespace-pre-wrap break-words text-sm text-slate-200">{shown}</p>
      {long && (
        <button onClick={() => setOpen((o) => !o)}
          className="mt-1 text-xs text-indigo-400 hover:underline focus:outline-none focus:ring-2 focus:ring-indigo-500 rounded">
          {open ? 'Show less' : 'Show full chunk'}
        </button>
      )}
    </div>
  );
}

export default function SearchModal({ onClose }) {
  const selectedId = useDocumentStore((s) => s.selectedId);
  const [q, setQ] = useState('');
  const [busy, setBusy] = useState(false);
  const [res, setRes] = useState(null);

  const run = async (e) => {
    e.preventDefault();
    if (!q.trim() || busy) return;
    setBusy(true);
    try {
      setRes(await chatApi.similarity({ query: q.trim(), topK: 8, ...(selectedId && { documentID: selectedId }) }));
    } catch (err) {
      setRes(null);
      toast.error(errMsg(err, 'Search failed'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/60 p-4" role="dialog" aria-modal="true" aria-label="Similarity search"
      onKeyDown={(e) => e.key === 'Escape' && onClose()}>
      <div className="flex max-h-[85vh] w-full max-w-2xl flex-col rounded-2xl border border-slate-700 bg-slate-900 shadow-2xl">
        <div className="flex items-center justify-between border-b border-slate-800 p-4">
          <h2 className="font-semibold text-slate-100">Semantic similarity search</h2>
          <button aria-label="Close search" onClick={onClose} className="text-slate-400 hover:text-slate-100"><X className="h-5 w-5" /></button>
        </div>
        <form onSubmit={run} className="flex gap-2 p-4">
          <input autoFocus value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search concepts, clauses, or facts..."
            className="flex-1 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          <button disabled={busy} className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 text-sm text-white hover:bg-indigo-500 disabled:opacity-60">
            {busy ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />} Search
          </button>
        </form>
        <div className="space-y-2 overflow-y-auto px-4 pb-4">
          {!res && !busy && <p className="py-8 text-center text-sm text-slate-400">Type a search term to inspect matching chunks.</p>}
          {res && !res.matches?.length && <p className="py-8 text-center text-sm text-slate-400">No matches found.</p>}
          {res?.matches?.length > 0 && (
            <p className="text-xs text-slate-400">{res.totalMatches ?? res.matches.length} matching chunks, best match first</p>
          )}
          {res?.matches?.map((m, i) => <MatchCard key={`${m.documentId}-${m.chunkIndex}-${i}`} m={m} rank={i + 1} />)}
        </div>
      </div>
    </div>
  );
}