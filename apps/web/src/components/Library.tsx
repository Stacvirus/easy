import { useI18n } from '../lib/i18n'
import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  ArrowRight,
  BookOpen,
  CalendarDays,
  CircleAlert,
  FilePlus2,
  FileText,
  FolderClosed,
  LoaderCircle,
  Plus,
  Search,
  Sparkles,
} from 'lucide-react'
import { api, type Subject } from '../lib/api'
import { errorText, labelDate, subjectName } from '../lib/presentation'

/** Owned document list and its subject, date and title filters. */
export function Library({
  token,
  subjects,
  subjectFilter,
  dateFilter,
  onDateFilterChange,
  search,
  onSearchChange,
  onSelectDocument,
  onCreateDocument,
}: {
  token: string
  subjects: Subject[]
  subjectFilter: string
  dateFilter: string
  onDateFilterChange: (value: string) => void
  search: string
  onSearchChange: (value: string) => void
  onSelectDocument: (documentId: string) => void
  onCreateDocument: () => void
}) {
  const { t } = useI18n()
  const documents = useQuery({
    queryKey: ['documents', subjectFilter, dateFilter],
    queryFn: () =>
      api.documents(
        token,
        !['all', 'general'].includes(subjectFilter) ? subjectFilter : undefined,
        dateFilter || undefined,
      ),
  })
  const visible = useMemo(
    () =>
      (documents.data ?? []).filter(
        (document) =>
          (subjectFilter !== 'general' || !document.subjectId) &&
          (document.title ?? '').toLowerCase().includes(search.toLowerCase()),
      ),
    [documents.data, search, subjectFilter],
  )
  return (
    <main className="content library-content">
      <div className="page-intro">
        <div>
          <span className="eyebrow">{t('YOUR LIBRARY')}</span>
          <h1>{t('Everything in its place.')}</h1>
          <p>
            {t(
              'Keep the original pages together, then turn them into an editable document.',
            )}
          </p>
        </div>
        <button
          className="button button-primary"
          onClick={() => onCreateDocument()}
        >
          <Plus size={17} /> {t('New document')}
        </button>
      </div>
      <div className="stats-strip">
        <div>
          <span className="stat-icon">
            <FolderClosed size={20} />
          </span>
          <div>
            <strong>{documents.data?.length ?? '—'}</strong>
            <span>{t('Documents')}</span>
          </div>
        </div>
        <div>
          <span className="stat-icon amber">
            <BookOpen size={20} />
          </span>
          <div>
            <strong>{subjects.length}</strong>
            <span>{t('Subjects')}</span>
          </div>
        </div>
        <div className="stat-note">
          <Sparkles size={17} /> {t('From image to editable Word file')}
        </div>
      </div>
      <div className="section-head">
        <div>
          <h2>
            {subjectFilter === 'all'
              ? t('All documents')
              : subjectFilter === 'general'
                ? t('General documents')
                : subjectName(subjects, subjectFilter)}
          </h2>
          <p>
            {visible.length}{' '}
            {visible.length === 1 ? t('document') : t('documents')}
          </p>
        </div>
        <div className="filters">
          <label className="search-field">
            <Search size={17} />
            <input
              value={search}
              onChange={(event) => onSearchChange(event.target.value)}
              placeholder={t('Search documents')}
              aria-label={t('Search documents')}
            />
          </label>
          <label className="date-field">
            <CalendarDays size={17} />
            <input
              type="date"
              value={dateFilter}
              onChange={(event) => onDateFilterChange(event.target.value)}
              aria-label={t('Filter by date')}
            />
          </label>
          {dateFilter && (
            <button
              className="clear-filter"
              onClick={() => onDateFilterChange('')}
            >
              {t('Clear')}
            </button>
          )}
        </div>
      </div>
      {documents.isLoading ? (
        <div className="empty-state">
          <LoaderCircle className="spin" size={30} />
          <p>{t('Loading your documents…')}</p>
        </div>
      ) : documents.isError ? (
        <div className="empty-state">
          <CircleAlert size={30} />
          <p>{errorText(documents.error)}</p>
          <button
            className="button button-secondary"
            onClick={() => void documents.refetch()}
          >
            {t('Try again')}
          </button>
        </div>
      ) : visible.length ? (
        <div className="document-grid">
          {visible.map((document) => (
            <button
              className="document-card"
              key={document.id}
              onClick={() => document.id && onSelectDocument(document.id)}
            >
              <span className="card-icon">
                <FileText size={25} />
              </span>
              <span className="card-meta">
                {subjectName(subjects, document.subjectId)}
              </span>
              <strong>{document.title}</strong>
              <span className="card-footer">
                <span>
                  <CalendarDays size={15} /> {labelDate(document.documentDate)}
                </span>
                <ArrowRight size={17} />
              </span>
            </button>
          ))}
        </div>
      ) : (
        <div className="empty-state">
          <div className="empty-illustration">
            <FilePlus2 size={31} />
          </div>
          <h3>
            {search || dateFilter || subjectFilter !== 'all'
              ? t('No documents match this view')
              : t('A fresh page awaits')}
          </h3>
          <p>
            {search || dateFilter || subjectFilter !== 'all'
              ? t('Try another search, date, or subject.')
              : t('Create a document, add your images, and start converting.')}
          </p>
          <button
            className="button button-primary"
            onClick={() => onCreateDocument()}
          >
            <Plus size={17} /> {t('New document')}
          </button>
        </div>
      )}
    </main>
  )
}
