import { useEffect, useRef, useState } from 'react'
import { Menu } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import type { Auth, Subject } from '../lib/api'
import { Sidebar } from './Sidebar'

/** Keep mobile drawer behavior with the navigation it controls. */
export function WorkspaceNavigation({
  auth,
  token,
  subjects,
  isLoading,
  subjectFilter,
  selectedDocument,
  onSelect,
  onSignOut,
  onMessage,
}: {
  auth: Auth
  token: string
  subjects: Subject[]
  isLoading: boolean
  subjectFilter: string
  selectedDocument: boolean
  onSelect: (filter: string) => void
  onSignOut: () => void
  onMessage: (text: string, error?: boolean) => void
}) {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  const menuButton = useRef<HTMLButtonElement>(null)

  function close() {
    setOpen(false)
    menuButton.current?.focus()
  }

  useEffect(() => {
    if (!open) return
    document
      .querySelector<HTMLButtonElement>('#workspace-navigation .nav-item')
      ?.focus()
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setOpen(false)
        menuButton.current?.focus()
      }
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [open])

  return (
    <>
      {open && (
        <button
          className="mobile-menu-backdrop"
          aria-label={t('workspace.closeMenu')}
          onClick={close}
        />
      )}
      <Sidebar
        auth={auth}
        token={token}
        allSubjects={subjects}
        isLoading={isLoading}
        subjectFilter={subjectFilter}
        selectedDocument={selectedDocument}
        mobileOpen={open}
        onMobileClose={close}
        onSelect={(filter) => {
          onSelect(filter)
          if (open) close()
        }}
        onSignOut={onSignOut}
        onMessage={onMessage}
      />
      <button
        ref={menuButton}
        className="mobile-menu-button"
        aria-label={t('workspace.openMenu')}
        aria-expanded={open}
        aria-controls="workspace-navigation"
        onClick={() => setOpen(true)}
      >
        <Menu size={21} />
      </button>
    </>
  )
}
