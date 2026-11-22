import { useTranslation } from 'react-i18next'
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
  const { t } = useTranslation()
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
          <span className="eyebrow">{t('library.eyebrow')}</span>
          <h1>{t('library.title')}</h1>
          <p>{t('library.description')}</p>
        </div>
        <button
          className="button button-primary"
          onClick={() => onCreateDocument()}
        >
          <Plus size={17} /> {t('common.newDocument')}
        </button>
      </div>
      <div className="stats-strip">
        <div>
          <span className="stat-icon">
            <FolderClosed size={20} />
          </span>
          <div>
            <strong>{documents.data?.length ?? '—'}</strong>
            <span>{t('library.documentsStat')}</span>
          </div>
        </div>
        <div>
          <span className="stat-icon amber">
            <BookOpen size={20} />
          </span>
          <div>
            <strong>{subjects.length}</strong>
            <span>{t('library.subjectsStat')}</span>
          </div>
        </div>
        <div className="stat-note">
          <Sparkles size={17} /> {t('library.statNote')}
        </div>
      </div>
      <div className="section-head">
        <div>
          <h2>
            {subjectFilter === 'all'
              ? t('library.allDocuments')
              : subjectFilter === 'general'
                ? t('library.generalDocuments')
                : subjectName(subjects, subjectFilter)}
          </h2>
          <p>{t('library.count', { count: visible.length })}</p>
        </div>
        <div className="filters">
          <label className="search-field">
            <Search size={17} />
            <input
              value={search}
              onChange={(event) => onSearchChange(event.target.value)}
              placeholder={t('library.search')}
              aria-label={t('library.search')}
            />
          </label>
          <label className="date-field">
            <CalendarDays size={17} />
            <input
              type="date"
              value={dateFilter}
              onChange={(event) => onDateFilterChange(event.target.value)}
              aria-label={t('library.filterByDate')}
            />
          </label>
          {dateFilter && (
            <button
              className="clear-filter"
              onClick={() => onDateFilterChange('')}
            >
              {t('common.clear')}
            </button>
          )}
        </div>
      </div>
      {documents.isLoading ? (
        <div className="empty-state">
          <LoaderCircle className="spin" size={30} />
          <p>{t('library.loading')}</p>
        </div>
      ) : documents.isError ? (
        <div className="empty-state">
          <CircleAlert size={30} />
          <p>{errorText(documents.error)}</p>
          <button
            className="button button-secondary"
            onClick={() => void documents.refetch()}
          >
            {t('common.tryAgain')}
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
              ? t('library.noMatches')
              : t('library.emptyTitle')}
          </h3>
          <p>
            {search || dateFilter || subjectFilter !== 'all'
              ? t('library.noMatchesHelp')
              : t('library.emptyHelp')}
          </p>
          <button
            className="button button-primary"
            onClick={() => onCreateDocument()}
          >
            <Plus size={17} /> {t('common.newDocument')}
          </button>
        </div>
      )}
    </main>
  )
}
