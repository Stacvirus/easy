import { useI18n } from '../lib/i18n'
import { useEffect, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, CalendarDays, CircleAlert, Trash2 } from 'lucide-react'
import { api, type Subject } from '../lib/api'
import { errorText, labelDate, subjectName } from '../lib/presentation'
import { DocumentPages } from './DocumentPages'
import { ConversionPanel } from './ConversionPanel'
import { ExportPanel } from './ExportPanel'

/** Load one owned document and coordinate its pages, jobs and exports. */
export function DocumentDetail({
  token,
  documentId,
  subjects,
  onBack,
  onMessage,
}: {
  token: string
  documentId: string
  subjects: Subject[]
  onBack: () => void
  onMessage: (text: string, error?: boolean) => void
}) {
  const { t } = useI18n()
  const queryClient = useQueryClient()
  const [busy, setBusy] = useState('')
  const document = useQuery({
    queryKey: ['document', documentId],
    queryFn: () => api.document(token, documentId),
  })
  const pages = useQuery({
    queryKey: ['pages', documentId],
    queryFn: () => api.pages(token, documentId),
  })
  const jobs = useQuery({
    queryKey: ['jobs', documentId],
    queryFn: () => api.conversions(token, documentId),
    refetchInterval: (query) =>
      query.state.data?.some(
        (job) => job.status === 'PENDING' || job.status === 'PROCESSING',
      )
        ? 2000
        : false,
  })
  const currentJob = jobs.data?.[0]
  const active =
    currentJob?.status === 'PENDING' || currentJob?.status === 'PROCESSING'
  const pageCount = pages.data?.length ?? 0
  useEffect(() => {
    if (currentJob?.status === 'COMPLETED')
      void queryClient.invalidateQueries({ queryKey: ['exports', documentId] })
  }, [currentJob?.id, currentJob?.status, documentId, queryClient])
  async function run(
    action: string,
    work: () => Promise<unknown>,
    success?: string,
  ) {
    setBusy(action)
    try {
      await work()
      if (success) onMessage(success)
    } catch (cause) {
      onMessage(errorText(cause), true)
    } finally {
      setBusy('')
    }
  }
  async function deleteDocument() {
    if (
      !window.confirm(t('Delete this document and all its pages and exports?'))
    )
      return
    await run(
      'delete-document',
      async () => {
        await api.deleteDocument(token, documentId)
        await queryClient.invalidateQueries({ queryKey: ['documents'] })
        onBack()
      },
      t('Document deleted.'),
    )
  }
  return (
    <main className="content detail-content">
      <button className="back-link" onClick={onBack}>
        <ArrowLeft size={17} /> {t('Back to library')}
      </button>
      {document.isError ? (
        <div className="empty-state">
          <CircleAlert size={30} />
          <p>{errorText(document.error)}</p>
        </div>
      ) : (
        <>
          <div className="detail-heading">
            <div>
              <span className="eyebrow">
                {t('DOCUMENT /')}{' '}
                {subjectName(subjects, document.data?.subjectId).toUpperCase()}
              </span>
              <h1>{document.data?.title || t('Loading document…')}</h1>
              <p>
                <CalendarDays size={16} />{' '}
                {labelDate(document.data?.documentDate)}{' '}
                <span className="bullet">·</span> {pageCount}{' '}
                {pageCount === 1 ? t('page') : t('pages')}
              </p>
            </div>
            <button
              className="button button-ghost danger-text"
              disabled={active || busy === 'delete-document'}
              onClick={() => void deleteDocument()}
            >
              <Trash2 size={16} /> {t('Delete document')}
            </button>
          </div>
          <div className="detail-grid">
            <DocumentPages
              token={token}
              documentId={documentId}
              pages={pages}
              busy={busy}
              run={run}
              onMessage={onMessage}
            />
            <aside className="detail-side">
              <ConversionPanel
                token={token}
                documentId={documentId}
                pageCount={pageCount}
                currentJob={currentJob}
                busy={busy}
                run={run}
              />
              <ExportPanel
                token={token}
                documentId={documentId}
                documentTitle={document.data?.title}
                busy={busy}
                run={run}
              />
            </aside>
          </div>
        </>
      )}
    </main>
  )
}
