import { useI18n } from '../lib/i18n'
import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { FileImage, LoaderCircle, X } from 'lucide-react'
import { api } from '../lib/api'
import { errorText } from '../lib/presentation'

export function ImagePreview({
  token,
  documentId,
  pageId,
  onClose,
}: {
  token: string
  documentId: string
  pageId: string
  onClose: () => void
}) {
  const { t } = useI18n()
  const image = useQuery({
    queryKey: ['page-image', documentId, pageId],
    queryFn: () => api.pageContent(token, documentId, pageId),
  })
  const [url, setUrl] = useState('')
  useEffect(() => {
    if (!image.data) return
    const next = URL.createObjectURL(image.data)
    setUrl(next)
    return () => URL.revokeObjectURL(next)
  }, [image.data])
  return (
    <div className="modal-backdrop preview-backdrop" onMouseDown={onClose}>
      <div
        className="preview-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={t('Original page preview')}
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="preview-header">
          <span>
            <FileImage size={18} /> {t('Original page')}
          </span>
          <button title={t('Close preview')} onClick={onClose}>
            <X size={21} />
          </button>
        </div>
        <div className="preview-body">
          {image.isLoading ? (
            <LoaderCircle className="spin" size={30} />
          ) : image.isError ? (
            <p className="error-text">{errorText(image.error)}</p>
          ) : url ? (
            <img src={url} alt={t('Original uploaded page')} />
          ) : null}
        </div>
      </div>
    </div>
  )
}
