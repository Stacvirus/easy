import { AuthScreen } from './components/AuthScreen'
import { Workspace } from './components/Workspace'
import type { Auth } from './lib/api'

export function App({
  auth,
  onAuth,
}: {
  auth: Auth | null
  onAuth: (auth: Auth | null) => void
}) {
  if (!auth?.accessToken) return <AuthScreen onAuth={onAuth} />
  return <Workspace auth={auth} onSignOut={() => onAuth(null)} />
}
