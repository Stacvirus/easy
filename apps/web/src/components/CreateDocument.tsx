import { useTranslation } from 'react-i18next'
import { useState, type FormEvent } from 'react'
import { CircleAlert, FilePlus2, X } from 'lucide-react'
import { api, type Document, type Subject } from '../lib/api'
import { errorText, today } from '../lib/presentation'

export function CreateDocument({
  token,
  subjects,
  initialSubject,
  onClose,
  onCreated,
}: {
  token: string
  subjects: Subject[]
  initialSubject: string
  onClose: () => void
  onCreated: (document: Document) => void
}) {
  const { t } = useTranslation()
  const [title, setTitle] = useState('')
  const [date, setDate] = useState(today())
  const [subjectId, setSubjectId] = useState(initialSubject)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  async function submit(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      onCreated(
        await api.createDocument(token, {
          title: title.trim(),
          documentDate: date,
          ...(subjectId ? { subjectId } : {}),
        }),
      )
    } catch (cause) {
      setError(errorText(cause))
    } finally {
      setBusy(false)
    }
  }
  return (
    <div className="modal-backdrop" onMouseDown={onClose}>
      <div
        className="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="create-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button
          className="dialog-close"
          aria-label={t('common.close')}
          onClick={onClose}
        >
          <X size={19} />
        </button>
        <span className="eyebrow">{t('createDocument.eyebrow')}</span>
        <h2 id="create-title">{t('createDocument.title')}</h2>
        <p className="muted">{t('createDocument.description')}</p>
        <form className="stack-form" onSubmit={submit}>
          <label>
            {t('createDocument.documentTitle')}
            <input
              autoFocus
              required
              maxLength={200}
              value={title}
              onChange={(event) => setTitle(event.target.value)}
              placeholder={t('createDocument.titleExample')}
            />
          </label>
          <div className="form-grid">
            <label>
              {t('common.date')}
              <input
                required
                type="date"
                value={date}
                onChange={(event) => setDate(event.target.value)}
              />
            </label>
            <label>
              {t('common.subject')}
              <select
                value={subjectId}
                onChange={(event) => setSubjectId(event.target.value)}
              >
                <option value="">{t('common.general')}</option>
                {subjects.map((subject) => (
                  <option key={subject.id} value={subject.id}>
                    {subject.name}
                  </option>
                ))}
              </select>
            </label>
          </div>
          {error && (
            <div className="inline-error" role="alert">
              <CircleAlert size={16} />
              {error}
            </div>
          )}
          <div className="dialog-actions">
            <button
              className="button button-secondary"
              type="button"
              onClick={onClose}
            >
              {t('common.cancel')}
            </button>
            <button className="button button-primary" disabled={busy}>
              <FilePlus2 size={17} /> {t('createDocument.create')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
