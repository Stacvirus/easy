import { StrictMode, useState } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { App } from './App'
import type { Auth } from './lib/api'
import './lib/i18n'
import './index.css'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false, refetchOnWindowFocus: false } },
})

function Root() {
  const [auth, setAuth] = useState<Auth | null>(() => {
    try {
      const saved = sessionStorage.getItem('document-auth')
      const session = saved ? (JSON.parse(saved) as Auth) : null
      return session?.accessToken &&
        session.expiresAt &&
        new Date(session.expiresAt) > new Date()
        ? session
        : null
    } catch {
      return null
    }
  })
  function updateAuth(next: Auth | null) {
    queryClient.clear()
    if (next) sessionStorage.setItem('document-auth', JSON.stringify(next))
    else sessionStorage.removeItem('document-auth')
    setAuth(next)
  }
  return <App auth={auth} onAuth={updateAuth} />
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <Root />
    </QueryClientProvider>
  </StrictMode>,
)
