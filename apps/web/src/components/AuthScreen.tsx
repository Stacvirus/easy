import { useTranslation } from 'react-i18next'
import { useState, type FormEvent } from 'react'
import {
  ArrowRight,
  CircleAlert,
  Eye,
  EyeOff,
  Layers3,
  LoaderCircle,
} from 'lucide-react'
import { api, type Auth } from '../lib/api'
import { errorText } from '../lib/presentation'
import { LanguageSwitch } from './LanguageSwitch'

export function AuthScreen({ onAuth }: { onAuth: (auth: Auth) => void }) {
  const { t } = useTranslation()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  async function submit(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      onAuth(
        await (mode === 'login'
          ? api.login({ email, password })
          : api.register({ email, password })),
      )
    } catch (cause) {
      setError(errorText(cause))
    } finally {
      setBusy(false)
    }
  }
  return (
    <div className="auth-layout">
      <div className="auth-story">
        <div className="brand brand-light">
          <span className="brand-mark">
            <Layers3 size={21} />
          </span>
          Document Studio
        </div>
        <div className="auth-story-body">
          <span className="eyebrow light">{t('auth.storyEyebrow')}</span>
          <h1>
            {t('auth.storyTitleFirst')}
            <br />
            <em>{t('auth.storyTitleSecond')}</em>
          </h1>
          <p>{t('auth.storyDescription')}</p>
          <div className="story-steps">
            <span>{t('auth.stepCapture')}</span>
            <span>{t('auth.stepOrganize')}</span>
            <span>{t('auth.stepConvert')}</span>
          </div>
        </div>
        <span className="auth-footnote">{t('auth.storyFootnote')}</span>
      </div>
      <div className="auth-panel">
        <div className="auth-language">
          <LanguageSwitch />
        </div>
        <div className="auth-card">
          <span className="eyebrow">
            {t('auth.welcome')}{' '}
            {mode === 'login' ? t('auth.back') : t('auth.in')}
          </span>
          <h2>
            {mode === 'login' ? t('auth.signInTitle') : t('auth.registerTitle')}
          </h2>
          <p className="muted">
            {mode === 'login'
              ? t('auth.signInDescription')
              : t('auth.registerDescription')}
          </p>
          <form className="stack-form" onSubmit={submit}>
            <label>
              {t('auth.email')}
              <input
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@university.edu"
              />
            </label>
            <label>
              {t('auth.password')}
              <span className="password-field">
                <input
                  type={showPassword ? 'text' : 'password'}
                  autoComplete={
                    mode === 'login' ? 'current-password' : 'new-password'
                  }
                  minLength={mode === 'register' ? 8 : undefined}
                  required
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder={t('auth.passwordPlaceholder')}
                />
                <button
                  type="button"
                  className="password-toggle"
                  aria-label={t(
                    showPassword ? 'auth.hidePassword' : 'auth.showPassword',
                  )}
                  aria-pressed={showPassword}
                  onClick={() => setShowPassword((visible) => !visible)}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </span>
            </label>
            {error && (
              <div className="inline-error" role="alert">
                <CircleAlert size={16} />
                {error}
              </div>
            )}
            <button className="button button-primary full" disabled={busy}>
              {busy ? (
                <LoaderCircle className="spin" size={17} />
              ) : (
                <ArrowRight size={17} />
              )}
              {mode === 'login' ? t('auth.signIn') : t('auth.createAccount')}
            </button>
          </form>
          <div className="auth-switch">
            {mode === 'login'
              ? t('auth.newHere')
              : t('auth.alreadyHaveAccount')}{' '}
            <button
              className="text-button"
              onClick={() => {
                setMode(mode === 'login' ? 'register' : 'login')
                setError('')
              }}
            >
              {mode === 'login'
                ? t('auth.createAccountLink')
                : t('auth.signIn')}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
