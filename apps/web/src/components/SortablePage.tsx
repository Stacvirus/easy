import { useTranslation } from 'react-i18next'
import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import {
  ArrowDown,
  ArrowUp,
  FileImage,
  GripVertical,
  Image,
  Trash2,
} from 'lucide-react'
import type { Page } from '../lib/api'

export function SortablePage({
  page,
  index,
  total,
  disabled,
  onPreview,
  onMove,
  onDelete,
}: {
  page: Page
  index: number
  total: number
  disabled: boolean
  onPreview: () => void
  onMove: (offset: number) => void
  onDelete: () => void
}) {
  const { t } = useTranslation()
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: page.id as string, disabled })
  return (
    <div
      ref={setNodeRef}
      className={`page-row ${isDragging ? 'dragging' : ''}`}
      style={{ transform: CSS.Transform.toString(transform), transition }}
    >
      <button
        className="drag-handle"
        title={t('pages.dragToReorder')}
        aria-label={t('pages.dragPageAria', { number: index + 1 })}
        disabled={disabled}
        {...attributes}
        {...listeners}
      >
        <GripVertical size={18} />
      </button>
      <span className="page-number">{String(index + 1).padStart(2, '0')}</span>
      <span className="page-image-icon">
        <FileImage size={21} />
      </span>
      <button className="page-title" onClick={onPreview}>
        <strong>{t('pages.pageNumber', { number: index + 1 })}</strong>
        <span>
          {page.contentType === 'image/png'
            ? t('pages.pngImage')
            : t('pages.jpegImage')}{' '}
          ·{' '}
          {page.sizeBytes
            ? Math.max(1, Math.round(page.sizeBytes / 1024))
            : '—'}{' '}
          KB
        </span>
      </button>
      <div className="page-actions">
        <button
          title={t('pages.moveUp')}
          aria-label={t('pages.moveUpAria', { number: index + 1 })}
          disabled={disabled || index === 0}
          onClick={() => onMove(-1)}
        >
          <ArrowUp size={16} />
        </button>
        <button
          title={t('pages.moveDown')}
          aria-label={t('pages.moveDownAria', { number: index + 1 })}
          disabled={disabled || index === total - 1}
          onClick={() => onMove(1)}
        >
          <ArrowDown size={16} />
        </button>
        <button
          title={t('pages.preview')}
          aria-label={t('pages.previewAria', { number: index + 1 })}
          onClick={onPreview}
        >
          <Image size={16} />
        </button>
        <button
          title={t('pages.remove')}
          aria-label={t('pages.removeAria', { number: index + 1 })}
          disabled={disabled}
          onClick={onDelete}
        >
          <Trash2 size={16} />
        </button>
      </div>
    </div>
  )
}
