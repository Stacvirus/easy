import type { Subject } from './api'
import i18n from './i18n'

export const today = () => {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
export const labelDate = (value?: string) =>
  value
    ? new Date(`${value.slice(0, 10)}T12:00:00`).toLocaleDateString(
        i18n.resolvedLanguage,
        {
          day: 'numeric',
          month: 'short',
          year: 'numeric',
        },
      )
    : '—'
export const errorText = (error: unknown) =>
  error instanceof Error ? error.message : i18n.t('errors.generic')
export const subjectName = (subjects: Subject[], subjectId?: string) =>
  subjects.find((subject) => subject.id === subjectId)?.name ??
  i18n.t('common.general')
