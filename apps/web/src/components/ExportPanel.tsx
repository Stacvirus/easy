import { useTranslation } from 'react-i18next'
import { useQuery } from '@tanstack/react-query'
import { Download, FileText } from 'lucide-react'
import { api } from '../lib/api'
import { errorText, labelDate } from '../lib/presentation'

/** List and download exports only through the owned document route. */
export function ExportPanel({
  token,
  documentId,
  documentTitle,
  busy,
  run,
}: {
  token: string
  documentId: string
  documentTitle?: string
  busy: string
  run: (
    action: string,
    work: () => Promise<unknown>,
    success?: string,
  ) => Promise<void>
}) {
  const { t } = useTranslation()
  const exports = useQuery({
    queryKey: ['exports', documentId],
    queryFn: () => api.exports(token, documentId),
  })
  async function download(exportId: string) {
    await run('download', async () => {
      const blob = await api.download(token, documentId, exportId)
      const url = URL.createObjectURL(blob)
      const link = window.document.createElement('a')
      link.href = url
      link.download = `${documentTitle?.replace(/[^a-z0-9-_ ]/gi, '').trim() || 'document'}.docx`
      window.document.body.append(link)
      link.click()
      link.remove()
      window.setTimeout(() => URL.revokeObjectURL(url), 60000)
    })
  }
  return (
    <section className="panel export-panel">
      <div className="panel-heading">
        <div>
          <span className="eyebrow">{t('exports.eyebrow')}</span>
          <h2>{t('exports.title')}</h2>
        </div>
        <span className="count-badge">{exports.data?.length ?? 0}</span>
      </div>
      {exports.isLoading ? (
        <p className="muted small">{t('exports.loading')}</p>
      ) : exports.isError ? (
        <p className="error-text small">{errorText(exports.error)}</p>
      ) : exports.data?.length ? (
        <div className="export-list">
          {exports.data.map((file) => (
            <div className="export-item" key={file.id}>
              <span className="word-icon">
                <FileText size={20} />
              </span>
              <div>
                <strong>{t('exports.editableDocument')}</strong>
                <span>
                  {labelDate(file.createdAt)} ·{' '}
                  {file.sizeBytes
                    ? Math.max(1, Math.round(file.sizeBytes / 1024))
                    : '—'}{' '}
                  KB
                </span>
              </div>
              <button
                title={t('exports.download')}
                aria-label={t('exports.download')}
                className="download-button"
                disabled={busy === 'download'}
                onClick={() => file.id && void download(file.id)}
              >
                <Download size={18} />
              </button>
            </div>
          ))}
        </div>
      ) : (
        <div className="export-empty">
          <FileText size={23} />
          <p>{t('exports.empty')}</p>
        </div>
      )}
    </section>
  )
}
