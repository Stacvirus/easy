import { useTranslation } from 'react-i18next'

export function LanguageSwitch() {
  const { t, i18n } = useTranslation()
  return (
    <label className="language-switch">
      <span className="sr-only">{t('common.interfaceLanguage')}</span>
      <select
        aria-label={t('common.interfaceLanguage')}
        value={i18n.resolvedLanguage || 'en'}
        onChange={(event) => void i18n.changeLanguage(event.target.value)}
      >
        <option value="en">{t('common.english')}</option>
        <option value="fr">{t('common.french')}</option>
      </select>
    </label>
  )
}
