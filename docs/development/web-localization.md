# Web localization

The web app uses [i18next](https://www.i18next.com/) and [react-i18next](https://react.i18next.com/). English and French strings are bundled in `apps/web/src/locales/en.json` and `fr.json`. Both files use the same semantic keys, grouped by screen or feature.

`apps/web/src/lib/i18n.ts` initializes the libraries once. The language detector checks the saved `document-language` preference, then the browser language. English is the fallback. The language switch calls `i18n.changeLanguage`, which saves the preference and updates the page's `lang` attribute. API requests use the resolved language in `Accept-Language`; OCR's document-language selector is separate from the interface language.

Use `useTranslation()` and `t('section.key')` in React components. Add each new key to both locale files. Use i18next interpolation for variable text, for example `t('pages.removeConfirm', { number })`, and its `_one` / `_other` plural forms with `{ count }`. Outside React, import the initialized instance from `src/lib/i18n.ts` and call `i18n.t(...)`. The `src/i18next.d.ts` declaration gives TypeScript the English resource keys.
