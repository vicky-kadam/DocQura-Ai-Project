import { useEffect, useState } from 'react';
import Navbar from '../components/Navbar.jsx';
import Sidebar from '../components/Sidebar.jsx';
import ChatView from '../components/ChatView.jsx';
import SearchModal from '../components/SearchModal.jsx';
import { useDocumentStore } from '../store/documentStore';
import { useConversationStore } from '../store/conversationStore';

export default function Home() {
  const [searchOpen, setSearchOpen] = useState(false);
  const sidebarOpen = useDocumentStore((s) => s.sidebarOpen);
  const fetchDocuments = useDocumentStore((s) => s.fetchDocuments);
  const loadChats = useConversationStore((s) => s.loadChats);

  useEffect(() => {
    fetchDocuments().catch(() => {});
    loadChats();
  }, [fetchDocuments, loadChats]);

  useEffect(() => {
    const onKey = (e) => { if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') { e.preventDefault(); setSearchOpen(true); } };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  return (
    <div className="flex h-full flex-col">
      <Navbar onSearch={() => setSearchOpen(true)} />
      <div className="flex min-h-0 flex-1">
        {sidebarOpen && <Sidebar />}
        <ChatView />
      </div>
      {searchOpen && <SearchModal onClose={() => setSearchOpen(false)} />}
    </div>
  );
}
