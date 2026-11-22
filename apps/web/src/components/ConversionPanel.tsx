import { useI18n } from '../lib/i18n'
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
  const { t } = useI18n()
  const queryClient = useQueryClient()
  const [language, setLanguage] = useState<'en' | 'fr'>('en')
  const active =
    currentJob?.status === 'PENDING' || currentJob?.status === 'PROCESSING'
  return (
    <section className="panel conversion-panel">
      <div className="conversion-icon">
        <Sparkles size={23} />
      </div>
      <span className="eyebrow">{t('DIGITIZE')}</span>
      <h2>{t('Make it editable.')}</h2>
      <p>
        {t(
          'Recognize handwriting and create a Word file in your saved page order.',
        )}
      </p>
      <label className="field-label">
        {t('Document language')}
        <select
          value={language}
          onChange={(event) => setLanguage(event.target.value as 'en' | 'fr')}
        >
          <option value="en">{t('English')}</option>
          <option value="fr">{t('Français')}</option>
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
            t('Conversion queued. You can keep working while it runs.'),
          )
        }
      >
        {busy === 'convert' || active ? (
          <LoaderCircle className="spin" size={17} />
        ) : (
          <Sparkles size={17} />
        )}
        {active
          ? t('Converting…')
          : currentJob?.status === 'FAILED'
            ? t('Try conversion again')
            : t('Convert to Word')}
      </button>
      <p className="conversion-hint">
        {t(
          'Handwriting recognition is best effort. Check the Word file after download.',
        )}
      </p>
      {currentJob && (
        <div
          className={`job-status status-${currentJob.status?.toLowerCase()}`}
        >
          <span className="status-dot" />
          <div>
            <strong>
              {currentJob.status === 'PENDING'
                ? t('Waiting to start')
                : currentJob.status === 'PROCESSING'
                  ? t('Recognizing pages')
                  : currentJob.status === 'COMPLETED'
                    ? t('Conversion complete')
                    : t('Conversion failed')}
            </strong>
            <span>
              {currentJob.status === 'FAILED'
                ? currentJob.failureMessage || t('Please try again.')
                : currentJob.status === 'COMPLETED'
                  ? t('Your Word file is ready below.')
                  : t('This page updates automatically.')}
            </span>
          </div>
          {active && <LoaderCircle className="spin" size={17} />}
        </div>
      )}
    </section>
  )
}
