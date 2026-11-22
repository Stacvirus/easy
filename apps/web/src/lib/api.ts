import type { components } from './api-types'
import { getLanguage, translate } from './i18n'

type Schema = components['schemas']
export type Auth = Schema['AuthResponseDto']
export type Subject = Schema['SubjectResponseDto']
export type Document = Schema['DocumentResponseDto']
export type Page = Schema['PageResponseDto']
export type Job = Schema['ConversionJobResponseDto']
export type Export = Schema['DocumentExportResponseDto']

const base = import.meta.env.VITE_API_BASE_URL ?? ''

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
  ) {
    super(message)
  }
}

/** One browser HTTP boundary for JSON, private images and downloads. */
async function request<T>(
  path: string,
  options: RequestInit = {},
  token?: string,
): Promise<T> {
  const headers = new Headers(options.headers)
  headers.set('Accept-Language', getLanguage())
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (options.body && !(options.body instanceof FormData))
    headers.set('Content-Type', 'application/json')

  let response: Response
  try {
    response = await fetch(`${base}/api/v1${path}`, { ...options, headers })
  } catch {
    throw new ApiError(
      translate('Cannot reach the server. Check that the API is running.'),
      0,
    )
  }

  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as {
      message?: string
    } | null
    throw new ApiError(
      body?.message || `Request failed (${response.status})`,
      response.status,
    )
  }
  if (response.status === 204) return undefined as T
  if (response.headers.get('Content-Type')?.includes('application/json'))
    return response.json() as Promise<T>
  return response.blob() as Promise<T>
}

const json = (body: unknown) => JSON.stringify(body)
const id = (value: string) => encodeURIComponent(value)

export const api = {
  login: (body: Schema['LoginRequestDto']) =>
    request<Auth>('/auth/login', { method: 'POST', body: json(body) }),
  register: (body: Schema['RegisterRequestDto']) =>
    request<Auth>('/auth/register', { method: 'POST', body: json(body) }),
  subjects: (token: string) => request<Subject[]>('/subjects', {}, token),
  createSubject: (token: string, body: Schema['SubjectRequestDto']) =>
    request<Subject>('/subjects', { method: 'POST', body: json(body) }, token),
  renameSubject: (
    token: string,
    subjectId: string,
    body: Schema['SubjectRequestDto'],
  ) =>
    request<Subject>(
      `/subjects/${id(subjectId)}`,
      { method: 'PATCH', body: json(body) },
      token,
    ),
  deleteSubject: (token: string, subjectId: string) =>
    request<void>(`/subjects/${id(subjectId)}`, { method: 'DELETE' }, token),
  documents: (token: string, subjectId?: string, date?: string) => {
    const search = new URLSearchParams()
    if (subjectId) search.set('subjectId', subjectId)
    if (date) search.set('date', date)
    return request<Document[]>(
      `/documents${search.size ? `?${search}` : ''}`,
      {},
      token,
    )
  },
  document: (token: string, documentId: string) =>
    request<Document>(`/documents/${id(documentId)}`, {}, token),
  createDocument: (token: string, body: Schema['DocumentCreateRequestDto']) =>
    request<Document>(
      '/documents',
      { method: 'POST', body: json(body) },
      token,
    ),
  deleteDocument: (token: string, documentId: string) =>
    request<void>(`/documents/${id(documentId)}`, { method: 'DELETE' }, token),
  pages: (token: string, documentId: string) =>
    request<Page[]>(`/documents/${id(documentId)}/pages`, {}, token),
  uploadPages: (token: string, documentId: string, files: File[]) => {
    const body = new FormData()
    files.forEach((file) => body.append('files', file))
    return request<Page[]>(
      `/documents/${id(documentId)}/pages`,
      { method: 'POST', body },
      token,
    )
  },
  reorderPages: (token: string, documentId: string, pageIds: string[]) =>
    request<Page[]>(
      `/documents/${id(documentId)}/pages/order`,
      {
        method: 'PUT',
        body: json({ pageIds } satisfies Schema['PageOrderRequestDto']),
      },
      token,
    ),
  deletePage: (token: string, documentId: string, pageId: string) =>
    request<void>(
      `/documents/${id(documentId)}/pages/${id(pageId)}`,
      { method: 'DELETE' },
      token,
    ),
  pageContent: (token: string, documentId: string, pageId: string) =>
    request<Blob>(
      `/documents/${id(documentId)}/pages/${id(pageId)}/content`,
      {},
      token,
    ),
  conversions: (token: string, documentId: string) =>
    request<Job[]>(`/documents/${id(documentId)}/conversions`, {}, token),
  convert: (token: string, documentId: string, language: 'en' | 'fr') =>
    request<Job>(
      `/documents/${id(documentId)}/conversions`,
      {
        method: 'POST',
        body: json({ language } satisfies Schema['ConversionCreateRequestDto']),
      },
      token,
    ),
  exports: (token: string, documentId: string) =>
    request<Export[]>(`/documents/${id(documentId)}/exports`, {}, token),
  download: (token: string, documentId: string, exportId: string) =>
    request<Blob>(
      `/documents/${id(documentId)}/exports/${id(exportId)}/download`,
      {},
      token,
    ),
}
