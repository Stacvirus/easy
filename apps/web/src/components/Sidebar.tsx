import { useI18n } from '../lib/i18n'
import { useState, type FormEvent } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import {
  BookOpen,
  Check,
  FileText,
  FolderClosed,
  Layers3,
  LogOut,
  MoreHorizontal,
  Plus,
  Trash2,
} from 'lucide-react'
import { api, type Auth, type Subject } from '../lib/api'
import { errorText } from '../lib/presentation'

/** Workspace navigation and subject management. */
export function Sidebar({
  auth,
  token,
  allSubjects,
  isLoading,
  subjectFilter,
  selectedDocument,
  onSelect,
  onSignOut,
  onMessage,
}: {
  auth: Auth
  token: string
  allSubjects: Subject[]
  isLoading: boolean
  subjectFilter: string
  selectedDocument: boolean
  onSelect: (filter: string) => void
  onSignOut: () => void
  onMessage: (text: string, error?: boolean) => void
}) {
  const { t } = useI18n()
  const queryClient = useQueryClient()
  const [subjectDraft, setSubjectDraft] = useState('')
  const [addingSubject, setAddingSubject] = useState(false)
  const [editingSubject, setEditingSubject] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function saveSubject(event: FormEvent) {
    event.preventDefault()
    if (!subjectDraft.trim()) return
    setBusy(true)
    try {
      if (editingSubject)
        await api.renameSubject(token, editingSubject, {
          name: subjectDraft.trim(),
        })
      else await api.createSubject(token, { name: subjectDraft.trim() })
      await queryClient.invalidateQueries({ queryKey: ['subjects'] })
      setAddingSubject(false)
      setEditingSubject(null)
      setSubjectDraft('')
      onMessage(editingSubject ? t('Subject renamed.') : t('Subject created.'))
    } catch (cause) {
      onMessage(errorText(cause), true)
    } finally {
      setBusy(false)
    }
  }
  async function deleteSubject(subject: Subject) {
    if (
      !subject.id ||
      !window.confirm(
        `${t('Delete')} ${subject.name}? ${t('Documents assigned to it must be removed first.')}`,
      )
    )
      return
    try {
      await api.deleteSubject(token, subject.id)
      if (subjectFilter === subject.id) onSelect('all')
      await queryClient.invalidateQueries({ queryKey: ['subjects'] })
      onMessage(t('Subject deleted.'))
    } catch (cause) {
      onMessage(errorText(cause), true)
    }
  }

  return (
    <aside className="sidebar">
      <div className="brand">
        <span className="brand-mark">
          <Layers3 size={20} />
        </span>
        Document Studio
      </div>
      <div className="sidebar-section">
        <p className="sidebar-label">{t('WORKSPACE')}</p>
        <button
          className={`nav-item ${subjectFilter === 'all' && !selectedDocument ? 'active' : ''}`}
          onClick={() => {
            onSelect('all')
          }}
        >
          <FolderClosed size={17} /> {t('All documents')}
        </button>
        <button
          className={`nav-item ${subjectFilter === 'general' && !selectedDocument ? 'active' : ''}`}
          onClick={() => {
            onSelect('general')
          }}
        >
          <FileText size={17} /> {t('General')}
        </button>
      </div>
      <div className="sidebar-section subjects-section">
        <div className="sidebar-heading">
          <p className="sidebar-label">{t('SUBJECTS')}</p>
          <button
            title={t('Add subject')}
            className="icon-button"
            onClick={() => {
              setAddingSubject(true)
              setEditingSubject(null)
              setSubjectDraft('')
            }}
          >
            <Plus size={17} />
          </button>
        </div>
        {isLoading && <p className="sidebar-hint">{t('Loading subjects…')}</p>}
        {allSubjects.map((subject) => (
          <div key={subject.id} className="subject-row">
            <button
              className={`nav-item ${subjectFilter === subject.id && !selectedDocument ? 'active' : ''}`}
              onClick={() => {
                onSelect(subject.id ?? 'all')
              }}
            >
              <BookOpen size={17} />
              <span className="truncate">{subject.name}</span>
            </button>
            <button
              title={`Edit ${subject.name}`}
              className="subject-menu"
              onClick={() => {
                setEditingSubject(subject.id ?? null)
                setSubjectDraft(subject.name ?? '')
                setAddingSubject(true)
              }}
            >
              <MoreHorizontal size={16} />
            </button>
          </div>
        ))}
        {addingSubject && (
          <form className="subject-form" onSubmit={saveSubject}>
            <input
              autoFocus
              required
              value={subjectDraft}
              maxLength={100}
              onChange={(event) => setSubjectDraft(event.target.value)}
              placeholder={t('Subject name')}
              aria-label={t('Subject name')}
            />
            <div>
              <button className="small-action" disabled={busy} type="submit">
                <Check size={14} /> {t('Save')}
              </button>
              <button
                className="small-action subtle"
                type="button"
                onClick={() => {
                  setAddingSubject(false)
                  setEditingSubject(null)
                }}
              >
                {t('Cancel')}
              </button>
              {editingSubject && (
                <button
                  className="small-action danger"
                  type="button"
                  onClick={() => {
                    const subject = allSubjects.find(
                      (item) => item.id === editingSubject,
                    )
                    if (subject) void deleteSubject(subject)
                    setAddingSubject(false)
                    setEditingSubject(null)
                  }}
                >
                  <Trash2 size={14} />
                </button>
              )}
            </div>
          </form>
        )}
        {!addingSubject && (
          <button
            className="add-subject"
            onClick={() => {
              setAddingSubject(true)
              setSubjectDraft('')
            }}
          >
            <Plus size={16} /> {t('Add subject')}
          </button>
        )}
      </div>
      <div className="sidebar-bottom">
        <div className="account-avatar">
          {auth.email?.slice(0, 1).toUpperCase() || 'U'}
        </div>
        <div className="account-copy">
          <strong>{t('My workspace')}</strong>
          <span title={auth.email}>{auth.email}</span>
        </div>
        <button
          title={t('Sign out')}
          className="icon-button"
          onClick={onSignOut}
        >
          <LogOut size={17} />
        </button>
      </div>
    </aside>
  )
}
