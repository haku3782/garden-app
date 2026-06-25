import { ref } from 'vue'
import { translations, LANG_CODES } from '@/i18n/translations'

const STORAGE_KEY = 'garden-app-lang'

function detectLang() {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved && LANG_CODES.includes(saved)) return saved
  const b = navigator.language.toLowerCase()
  if (b.startsWith('zh-tw') || b.startsWith('zh-hk')) return 'zh-TW'
  if (b.startsWith('zh')) return 'zh'
  if (b.startsWith('ko')) return 'ko'
  if (b.startsWith('th')) return 'th'
  if (b.startsWith('ja')) return 'ja'
  return 'en'
}

// モジュールスコープで1つだけ持つことで、全コンポーネントが同じ言語状態を共有する
const lang = ref(detectLang())

function changeLang(code) {
  lang.value = code
  localStorage.setItem(STORAGE_KEY, code)
}

function t(key) {
  return translations[lang.value][key] ?? translations.ja[key] ?? key
}

const langList = LANG_CODES.map(code => ({
  code,
  name: translations[code].langName,
  flag: translations[code].flag,
  short: translations[code].short
}))

export function useLanguage() {
  return { lang, changeLang, t, langList }
}
