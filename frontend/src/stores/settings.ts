import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/api'
import type { AppSettingDto } from '@/api/api'
import { useUiStore } from './ui'
import { i18n } from '@/locales'

export const useSettingsStore = defineStore('settings', () => {
  const uiStore = useUiStore()
  const settings = ref<AppSettingDto>({})
  const t = i18n.global.t

  const isLoadingSettings = ref(false)
  const isSavingSettings = ref(false)

  const fetchSettings = async () => {
    isLoadingSettings.value = true
    try {
      settings.value = await api.settings.getSettings()
      console.log(`fetchSettings`, JSON.stringify(settings.value))
    } catch (e) {
      uiStore.showToast("Could not load settings from backend")
    } finally {
      isLoadingSettings.value = false
    }
  }

  const saveBackendSettings = async (settingsData: Partial<AppSettingDto>) => {
    isSavingSettings.value = true
    try {
      settings.value = await api.settings.updateSettings(settingsData)
    } catch (err: any) {
      uiStore.showToast("Could not save settings to backend")
    } finally {
      uiStore.showToast(t('settings.savedToast'))
      isSavingSettings.value = false
    }
  }

  return {
    settings,
    isLoadingSettings,
    isSavingSettings,
    fetchSettings,
    saveBackendSettings,
  }
})
