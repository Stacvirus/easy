import { useTranslation } from 'react-i18next'
import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { LoaderCircle, Sparkles } from 'lucide-react'
import { api, type Job } from '../lib/api'

/** Start an OCR job and show its latest persisted status. */
export function ConversionPanel({
  token,
  documentId,
  pageCount,
  currentJob,
  busy,
  run,
}: {
  token: string
  documentId: string
  pageCount: number
  currentJob?: Job
  busy: string
  run: (
    action: string,
    work: () => Promise<unknown>,
    success?: string,
  ) => Promise<void>
}) {
  const { t } = useTranslation()
  const queryClient = useQueryClient()
  const [language, setLanguage] = useState<'en' | 'fr'>('en')
  const active =
    currentJob?.status === 'PENDING' || currentJob?.status === 'PROCESSING'
  return (
    <section className="panel conversion-panel">
      <div className="conversion-icon">
        <Sparkles size={23} />
      </div>
      <span className="eyebrow">{t('conversion.eyebrow')}</span>
      <h2>{t('conversion.title')}</h2>
      <p>{t('conversion.description')}</p>
      <label className="field-label">
        {t('conversion.documentLanguage')}
        <select
          value={language}
          onChange={(event) => setLanguage(event.target.value as 'en' | 'fr')}
        >
          <option value="en">{t('common.english')}</option>
          <option value="fr">{t('common.french')}</option>
        </select>
      </label>
      <button
        className="button button-primary full"
        disabled={!pageCount || !!busy || active}
        onClick={() =>
          void run(
            'convert',
            async () => {
              await api.convert(token, documentId, language)
              await queryClient.invalidateQueries({
                queryKey: ['jobs', documentId],
              })
            },
            t('conversion.queued'),
          )
        }
      >
        {busy === 'convert' || active ? (
          <LoaderCircle className="spin" size={17} />
        ) : (
          <Sparkles size={17} />
        )}
        {active
          ? t('conversion.converting')
          : currentJob?.status === 'FAILED'
            ? t('conversion.retry')
            : t('conversion.convert')}
      </button>
      <p className="conversion-hint">{t('conversion.accuracyHint')}</p>
      {currentJob && (
        <div
          className={`job-status status-${currentJob.status?.toLowerCase()}`}
        >
          <span className="status-dot" />
          <div>
            <strong>
              {currentJob.status === 'PENDING'
                ? t('conversion.pending')
                : currentJob.status === 'PROCESSING'
                  ? t('conversion.processing')
                  : currentJob.status === 'COMPLETED'
                    ? t('conversion.completed')
                    : t('conversion.failed')}
            </strong>
            <span>
              {currentJob.status === 'FAILED'
                ? currentJob.failureMessage || t('common.tryAgainMessage')
                : currentJob.status === 'COMPLETED'
                  ? t('conversion.fileReady')
                  : t('conversion.autoUpdates')}
            </span>
          </div>
          {active && <LoaderCircle className="spin" size={17} />}
        </div>
      )}
    </section>
  )
}
