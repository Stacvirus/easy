import { useTranslation } from 'react-i18next'
import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import {
  CalendarDays,
  Check,
  CircleAlert,
  Layers3,
  LogOut,
  X,
} from 'lucide-react'
import { DocumentDetail } from './DocumentDetail'
import { api, type Auth } from '../lib/api'
import { labelDate, today } from '../lib/presentation'
import { CreateDocument } from './CreateDocument'
import { WorkspaceNavigation } from './WorkspaceNavigation'
import { Library } from './Library'
import { LanguageSwitch } from './LanguageSwitch'

type Notice = { text: string; error?: boolean }

export function Workspace({
  auth,
  onSignOut,
}: {
  auth: Auth
  onSignOut: () => void
}) {
  const { t } = useTranslation()
  const token = auth.accessToken as string
  const queryClient = useQueryClient()
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [subjectFilter, setSubjectFilter] = useState('all')
  const [dateFilter, setDateFilter] = useState('')
  const [search, setSearch] = useState('')
  const [showCreate, setShowCreate] = useState(false)
  const [notice, setNotice] = useState<Notice | null>(null)
  const subjects = useQuery({
    queryKey: ['subjects'],
    queryFn: () => api.subjects(token),
  })
  const allSubjects = subjects.data ?? []
  const message = (text: string, error = false) => setNotice({ text, error })
  return (
    <div className="app-shell">
      <WorkspaceNavigation
        auth={auth}
        token={token}
        subjects={allSubjects}
        isLoading={subjects.isLoading}
        subjectFilter={subjectFilter}
        selectedDocument={!!selectedId}
        onSelect={(filter) => {
          setSelectedId(null)
          setSubjectFilter(filter)
        }}
        onSignOut={onSignOut}
        onMessage={message}
      />
      <div className="main-area">
        <header className="topbar">
          <span className="mobile-brand">
            <span className="brand-mark">
              <Layers3 size={18} />
            </span>{' '}
            Document Studio
          </span>
          <span className="topbar-caption">{t('workspace.topbarCaption')}</span>
          <div className="topbar-right">
            <LanguageSwitch />
            <span className="topbar-date">
              <CalendarDays size={15} /> {labelDate(today())}
            </span>
            <span className="topbar-avatar">
              {auth.email?.slice(0, 1).toUpperCase() || 'U'}
            </span>
            <button
              title={t('common.signOut')}
              aria-label={t('common.signOut')}
              className="mobile-signout"
              onClick={onSignOut}
            >
              <LogOut size={17} />
            </button>
          </div>
        </header>
        {notice && (
          <div
            className={`toast ${notice.error ? 'toast-error' : ''}`}
            role="status"
          >
            <span>
              {notice.error ? <CircleAlert size={17} /> : <Check size={17} />}
              {notice.text}
            </span>
            <button title={t('common.dismiss')} onClick={() => setNotice(null)}>
              <X size={16} />
            </button>
          </div>
        )}
        {selectedId ? (
          <DocumentDetail
            token={token}
            documentId={selectedId}
            subjects={allSubjects}
            onBack={() => setSelectedId(null)}
            onMessage={message}
          />
        ) : (
          <Library
            token={token}
            subjects={allSubjects}
            subjectFilter={subjectFilter}
            dateFilter={dateFilter}
            onDateFilterChange={setDateFilter}
            search={search}
            onSearchChange={setSearch}
            onSelectDocument={setSelectedId}
            onCreateDocument={() => setShowCreate(true)}
          />
        )}
      </div>
      {showCreate && (
        <CreateDocument
          token={token}
          subjects={allSubjects}
          initialSubject={
            !['all', 'general'].includes(subjectFilter) ? subjectFilter : ''
          }
          onClose={() => setShowCreate(false)}
          onCreated={(document) => {
            setShowCreate(false)
            void queryClient.invalidateQueries({ queryKey: ['documents'] })
            if (document.id) setSelectedId(document.id)
            message(t('workspace.documentCreated'))
          }}
        />
      )}
    </div>
  )
}
