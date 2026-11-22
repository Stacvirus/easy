import { useI18n } from '../lib/i18n'
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
  const { t } = useI18n()
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
        title={t('Drag to reorder')}
        aria-label={`${t('Drag page')} ${index + 1} ${t('to reorder')}`}
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
        <strong>
          {t('Page')} {index + 1}
        </strong>
        <span>
          {page.contentType === 'image/png' ? t('PNG image') : t('JPEG image')}{' '}
          ·{' '}
          {page.sizeBytes
            ? Math.max(1, Math.round(page.sizeBytes / 1024))
            : '—'}{' '}
          KB
        </span>
      </button>
      <div className="page-actions">
        <button
          title={t('Move up')}
          aria-label={`${t('Move page')} ${index + 1} ${t('up')}`}
          disabled={disabled || index === 0}
          onClick={() => onMove(-1)}
        >
          <ArrowUp size={16} />
        </button>
        <button
          title={t('Move down')}
          aria-label={`${t('Move page')} ${index + 1} ${t('down')}`}
          disabled={disabled || index === total - 1}
          onClick={() => onMove(1)}
        >
          <ArrowDown size={16} />
        </button>
        <button
          title={t('Preview')}
          aria-label={`${t('Preview page')} ${index + 1}`}
          onClick={onPreview}
        >
          <Image size={16} />
        </button>
        <button
          title={t('Remove page')}
          aria-label={`${t('Remove page')} ${index + 1}`}
          disabled={disabled}
          onClick={onDelete}
        >
          <Trash2 size={16} />
        </button>
      </div>
    </div>
  )
}
