import { createI18n } from 'vue-i18n'
import en from './en.json'
import ua from './ua.json'

export type LocaleKey = 'ua' | 'en'

const savedLocale = (localStorage.getItem('parts_manager_locale') as LocaleKey) || 'ua'

export const i18n = createI18n({
  legacy: false,
  locale: savedLocale === 'en' ? 'en' : 'ua',
  fallbackLocale: 'en',
  messages: {
    ua,
    en,
  },
})

export default i18n
