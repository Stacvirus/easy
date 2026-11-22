import i18n from 'i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import { initReactI18next } from 'react-i18next'
import en from '../locales/en.json'
import fr from '../locales/fr.json'

void i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      fr: { translation: fr },
    },
    fallbackLng: 'en',
    supportedLngs: ['en', 'fr'],
    load: 'languageOnly',
    initAsync: false,
    detection: {
      order: ['localStorage', 'navigator'],
      lookupLocalStorage: 'document-language',
      caches: ['localStorage'],
    },
    interpolation: { escapeValue: false },
  })

function updateDocumentLanguage(language: string) {
  document.documentElement.lang = language.startsWith('fr') ? 'fr' : 'en'
}

i18n.on('languageChanged', updateDocumentLanguage)
updateDocumentLanguage(i18n.resolvedLanguage || 'en')

export default i18n
