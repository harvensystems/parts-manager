import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  // Navigation & Active View
  const currentView = ref<'catalog' | 'queue' | 'settings'>('catalog')
  const activeUploadTab = ref<'manual' | 'auto'>('manual')

  // Modals visibility
  const showUploadModal = ref(false)
  const showDetailModal = ref(false)

  // Toast notifications
  const toastMessage = ref('')
  let toastTimeout: any = null

  const showToast = (msg: string) => {
    toastMessage.value = msg
    if (toastTimeout) clearTimeout(toastTimeout)
    toastTimeout = setTimeout(() => {
      toastMessage.value = ''
    }, 3000)
  }

  // Theme management
  const isDarkMode = ref(false)
  const applyTheme = () => isDarkMode.value ? document.documentElement.classList.add('dark') : document.documentElement.classList.remove('dark')

  const toggleTheme = () => {
    isDarkMode.value = !isDarkMode.value
    localStorage.setItem('part_manager_theme', isDarkMode.value ? 'dark' : 'light')
    applyTheme()
  }

  const setTheme = (dark: boolean) => {
    isDarkMode.value = dark
    localStorage.setItem('part_manager_theme', dark ? 'dark' : 'light')
    applyTheme()
  }

  const initTheme = () => {
    const savedTheme = localStorage.getItem('part_manager_theme')
    if (savedTheme) {
      isDarkMode.value = savedTheme === 'dark'
    } else {
      isDarkMode.value = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyTheme()
  }

  return {
    currentView,
    showUploadModal,
    showDetailModal,
    activeUploadTab,
    toastMessage,
    showToast,
    isDarkMode,
    toggleTheme,
    setTheme,
    initTheme,
    applyTheme,
  }
})
