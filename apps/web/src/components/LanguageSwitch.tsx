import { useI18n, type Language } from '../lib/i18n'

export function LanguageSwitch() {
  const { language, setLanguage } = useI18n()
  return (
    <label className="language-switch">
      <span className="sr-only">
        {language === 'fr' ? 'Langue' : 'Language'}
      </span>
      <select
        aria-label={
          language === 'fr' ? 'Langue de l’interface' : 'Interface language'
        }
        value={language}
        onChange={(event) => setLanguage(event.target.value as Language)}
      >
        <option value="en">English</option>
        <option value="fr">Français</option>
      </select>
    </label>
  )
}
