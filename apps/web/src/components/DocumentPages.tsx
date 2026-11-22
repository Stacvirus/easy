import { useI18n } from '../lib/i18n'
import { useRef, useState } from 'react'
import {
  DndContext,
  KeyboardSensor,
  PointerSensor,
  closestCenter,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core'
import {
  SortableContext,
  arrayMove,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { useQueryClient, type UseQueryResult } from '@tanstack/react-query'
import { Image, LoaderCircle, UploadCloud } from 'lucide-react'
import { api, type Page } from '../lib/api'
import { errorText } from '../lib/presentation'
import { ImagePreview } from './ImagePreview'
import { SortablePage } from './SortablePage'

/** Upload, order, preview and remove the source pages of one document. */
export function DocumentPages({
  token,
  documentId,
  pages,
  busy,
  run,
  onMessage,
}: {
  token: string
  documentId: string
  pages: UseQueryResult<Page[], Error>
  busy: string
  run: (
    action: string,
    work: () => Promise<unknown>,
    success?: string,
  ) => Promise<void>
  onMessage: (text: string, error?: boolean) => void
}) {
  const { t } = useI18n()
  const queryClient = useQueryClient()
  const fileInput = useRef<HTMLInputElement>(null)
  const [previewId, setPreviewId] = useState<string | null>(null)
  const pageList = pages.data ?? []
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    }),
  )
  function upload(files: FileList | File[]) {
    const selected = Array.from(files)
    if (!selected.length) return
    if (
      selected.some(
        (file) =>
          !['image/jpeg', 'image/png'].includes(file.type) ||
          file.size > 10 * 1024 * 1024,
      )
    ) {
      onMessage(t('Use JPEG or PNG images under 10 MB each.'), true)
      return
    }
    if (
      selected.reduce((size, file) => size + file.size, 0) >
      50 * 1024 * 1024
    ) {
      onMessage(t('Upload at most 50 MB at a time.'), true)
      return
    }
    if (pageList.length + selected.length > 50) {
      onMessage(t('A document can have at most 50 pages.'), true)
      return
    }
    void run(
      'upload',
      async () => {
        await api.uploadPages(token, documentId, selected)
        await queryClient.invalidateQueries({ queryKey: ['pages', documentId] })
      },
      `${selected.length} ${selected.length === 1 ? t('page added.') : t('pages added.')}`,
    )
  }
  function reorder(next: Page[]) {
    if (next.some((page) => !page.id)) return
    void run(
      'reorder',
      async () => {
        await api.reorderPages(
          token,
          documentId,
          next.map((page) => page.id as string),
        )
        await queryClient.invalidateQueries({ queryKey: ['pages', documentId] })
      },
      t('Page order saved.'),
    )
  }
  function dragEnd(event: DragEndEvent) {
    if (!event.over || event.active.id === event.over.id) return
    const from = pageList.findIndex((page) => page.id === event.active.id)
    const to = pageList.findIndex((page) => page.id === event.over?.id)
    if (from >= 0 && to >= 0) reorder(arrayMove(pageList, from, to))
  }
  return (
    <>
      <section className="panel pages-panel">
        <div className="panel-heading">
          <div>
            <span className="eyebrow">{t('ORIGINALS')}</span>
            <h2>{t('Document pages')}</h2>
            <p>{t('Your source images stay available after conversion.')}</p>
          </div>
          <span className="count-badge">{pageList.length} / 50</span>
        </div>
        <div
          className={`upload-zone ${busy === 'upload' ? 'uploading' : ''}`}
          onDragOver={(event) => event.preventDefault()}
          onDrop={(event) => {
            event.preventDefault()
            if (!busy) upload(event.dataTransfer.files)
          }}
        >
          <input
            ref={fileInput}
            type="file"
            accept="image/jpeg,image/png"
            multiple
            hidden
            onChange={(event) => {
              if (event.target.files) upload(event.target.files)
              event.target.value = ''
            }}
          />
          <div className="upload-icon">
            <UploadCloud size={24} />
          </div>
          <div>
            <strong>
              {busy === 'upload'
                ? t('Uploading pages…')
                : t('Drop images here, or browse')}
            </strong>
            <span>
              {t('JPEG or PNG · Up to 10 MB each · 50 pages maximum')}
            </span>
          </div>
          <button
            className="button button-secondary"
            disabled={!!busy || pageList.length >= 50}
            onClick={() => fileInput.current?.click()}
          >
            {t('Browse files')}
          </button>
        </div>
        {pages.isLoading ? (
          <div className="panel-empty">
            <LoaderCircle className="spin" size={22} /> {t('Loading pages…')}
          </div>
        ) : pages.isError ? (
          <div className="panel-empty error-text">{errorText(pages.error)}</div>
        ) : pageList.length ? (
          <>
            <div className="page-list-head">
              <span>{t('PAGE ORDER')}</span>
              <span>{t('Drag or use arrows to arrange')}</span>
            </div>
            <DndContext
              sensors={sensors}
              collisionDetection={closestCenter}
              onDragEnd={dragEnd}
            >
              <SortableContext
                items={pageList.map((page) => page.id as string)}
                strategy={verticalListSortingStrategy}
              >
                <div className="page-list">
                  {pageList.map((page, index) => (
                    <SortablePage
                      key={page.id}
                      page={page}
                      index={index}
                      total={pageList.length}
                      disabled={!!busy}
                      onPreview={() => page.id && setPreviewId(page.id)}
                      onMove={(offset) =>
                        reorder(arrayMove(pageList, index, index + offset))
                      }
                      onDelete={() => {
                        if (
                          !page.id ||
                          !window.confirm(`${t('Remove page')} ${index + 1}?`)
                        )
                          return
                        void run(
                          'delete-page',
                          async () => {
                            await api.deletePage(
                              token,
                              documentId,
                              page.id as string,
                            )
                            await queryClient.invalidateQueries({
                              queryKey: ['pages', documentId],
                            })
                          },
                          t('Page removed.'),
                        )
                      }}
                    />
                  ))}
                </div>
              </SortableContext>
            </DndContext>
          </>
        ) : (
          <div className="panel-empty">
            <Image size={27} />
            <strong>{t('No pages yet')}</strong>
            <span>{t('Add your first image to start this document.')}</span>
          </div>
        )}
      </section>
      {previewId && (
        <ImagePreview
          token={token}
          documentId={documentId}
          pageId={previewId}
          onClose={() => setPreviewId(null)}
        />
      )}
    </>
  )
}
