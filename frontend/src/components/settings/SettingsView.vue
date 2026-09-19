<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-200 dark:border-slate-800">
      <div>
        <h2 class="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
          <i data-lucide="sliders" class="w-6 h-6 text-emerald-600 dark:text-emerald-400"></i>
          {{ t('settings.title') }}
        </h2>
        <p class="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-0.5">
          {{ t('settings.subtitle') }}
        </p>
      </div>

      <!-- Action Buttons -->
      <div class="flex items-center gap-2">
        <button
          type="button"
          @click="resetDefaults"
          class="px-3 py-2 bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 text-xs font-semibold rounded-lg border border-slate-300 dark:border-slate-700 transition cursor-pointer"
        >
          {{ t('settings.resetDefaults') }}
        </button>
        <button
          type="button"
          @click="saveSettings"
          :disabled="settingsStore.isSavingSettings"
          class="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white dark:text-slate-950 text-xs sm:text-sm font-bold rounded-lg shadow-md transition flex items-center gap-2 cursor-pointer disabled:opacity-50"
        >
          <i v-if="!settingsStore.isSavingSettings" data-lucide="check" class="w-4 h-4"></i>
          <span v-if="settingsStore.isSavingSettings">{{ t('settings.saving') }}</span>
          <span v-else>{{ t('settings.save') }}</span>
        </button>
      </div>
    </div>

    <!-- Interface & Appearance Section -->
    <div class="glass-card rounded-xl p-4 sm:p-6 space-y-5 shadow-xs">
      <div class="flex items-center gap-3 border-b border-slate-200 dark:border-slate-800/80 pb-3">
        <div class="p-2 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 rounded-lg">
          <i data-lucide="palette" class="w-5 h-5"></i>
        </div>
        <div>
          <h3 class="font-bold text-sm sm:text-base text-slate-900 dark:text-white">
            {{ t('settings.interfaceSection') }}
          </h3>
          <p class="text-[11px] sm:text-xs text-slate-500 dark:text-slate-400">
            {{ t('settings.interfaceDesc') }}
          </p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
        <!-- Language Option -->
        <div class="space-y-2">
          <label class="block text-xs font-semibold text-slate-800 dark:text-slate-200">
            {{ t('settings.language') }}
          </label>
          <div class="grid grid-cols-2 gap-2">
            <button
              type="button"
              @click="changeLocale('ua')"
              class="px-3 py-2.5 rounded-lg border text-xs font-semibold flex items-center justify-center gap-2 transition cursor-pointer"
              :class="locale === 'ua' ? 'bg-emerald-500/15 border-emerald-500 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'bg-slate-50 dark:bg-slate-900 border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-slate-400'"
            >
              <span>🇺🇦</span>
              <span>Українська</span>
            </button>
            <button
              type="button"
              @click="changeLocale('en')"
              class="px-3 py-2.5 rounded-lg border text-xs font-semibold flex items-center justify-center gap-2 transition cursor-pointer"
              :class="locale === 'en' ? 'bg-emerald-500/15 border-emerald-500 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'bg-slate-50 dark:bg-slate-900 border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-slate-400'"
            >
              <span>🇬🇧</span>
              <span>English</span>
            </button>
          </div>
          <p class="text-[11px] text-slate-500 dark:text-slate-500">{{ t('settings.languageHelp') }}</p>
        </div>

        <!-- Theme Option -->
        <div class="space-y-2">
          <label class="block text-xs font-semibold text-slate-800 dark:text-slate-200">
            {{ t('settings.theme') }}
          </label>
          <div class="grid grid-cols-2 gap-2">
            <button
              type="button"
              @click="uiStore.setTheme(true)"
              class="px-3 py-2.5 rounded-lg border text-xs font-semibold flex items-center justify-center gap-2 transition cursor-pointer"
              :class="uiStore.isDarkMode ? 'bg-emerald-500/15 border-emerald-500 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'bg-slate-50 dark:bg-slate-900 border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-slate-400'"
            >
              <i data-lucide="moon" class="w-4 h-4 text-amber-500 dark:text-amber-400"></i>
              <span>{{ t('settings.themeDark') }}</span>
            </button>
            <button
              type="button"
              @click="uiStore.setTheme(false)"
              class="px-3 py-2.5 rounded-lg border text-xs font-semibold flex items-center justify-center gap-2 transition cursor-pointer"
              :class="!uiStore.isDarkMode ? 'bg-emerald-500/15 border-emerald-500 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'bg-slate-50 dark:bg-slate-900 border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-slate-400'"
            >
              <i data-lucide="sun" class="w-4 h-4 text-amber-500"></i>
              <span>{{ t('settings.themeLight') }}</span>
            </button>
          </div>
          <p class="text-[11px] text-slate-500 dark:text-slate-500">{{ t('settings.themeHelp') }}</p>
        </div>
      </div>
    </div>

    <!-- Inventory & Stock Rules (Backend) -->
    <div class="glass-card rounded-xl p-4 sm:p-6 space-y-5 shadow-xs">
      <div class="flex items-center gap-3 border-b border-slate-200 dark:border-slate-800/80 pb-3">
        <div class="p-2 bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 rounded-lg">
          <i data-lucide="boxes" class="w-5 h-5"></i>
        </div>
        <div>
          <h3 class="font-bold text-sm sm:text-base text-slate-900 dark:text-white">
            {{ t('settings.inventorySection') }}
          </h3>
          <p class="text-[11px] sm:text-xs text-slate-500 dark:text-slate-400">
            {{ t('settings.inventoryDesc') }}
          </p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 sm:gap-6">
        <!-- Low Stock Threshold -->
        <div class="space-y-1.5">
          <div class="flex items-center justify-between">
            <label class="block text-xs font-semibold text-slate-800 dark:text-slate-200">
              {{ t('settings.lowStock') }}
            </label>
            <span class="font-mono text-xs font-bold text-rose-600 dark:text-rose-400 bg-rose-50 dark:bg-rose-950/60 px-2 py-0.5 rounded border border-rose-200 dark:border-rose-900">
              &le; {{ localLowStock }} {{ t('stats.pcs') }}
            </span>
          </div>
          <input
            v-model.number="localLowStock"
            type="number"
            min="0"
            max="1000"
            class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white font-mono text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
          >
          <p class="text-[11px] text-slate-500 dark:text-slate-500">{{ t('settings.lowStockHelp') }}</p>
        </div>

        <!-- Default Page Size -->
        <div class="space-y-1.5">
          <div class="flex items-center justify-between">
            <label class="block text-xs font-semibold text-slate-800 dark:text-slate-200">
              {{ t('settings.defaultPageSize') }}
            </label>
            <span class="font-mono text-xs font-bold text-slate-700 dark:text-slate-300 bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded">
              {{ localDefaultPageSize }}
            </span>
          </div>
          <select
            v-model.number="localDefaultPageSize"
            class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
          >
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
            <option :value="100">100</option>
          </select>
          <p class="text-[11px] text-slate-500 dark:text-slate-500">{{ t('settings.defaultPageSizeHelp') }}</p>
        </div>

        <!-- Image Quality -->
        <div class="space-y-1.5">
          <div class="flex items-center justify-between">
            <label class="block text-xs font-semibold text-slate-800 dark:text-slate-200">
              {{ t('settings.imageQuality') }}
            </label>
            <span class="font-mono text-xs font-bold text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-200 dark:border-emerald-900">
              {{ localImageQuality }}%
            </span>
          </div>
          <input
            v-model.number="localImageQuality"
            type="range"
            min="30"
            max="100"
            step="5"
            class="w-full accent-emerald-500 cursor-pointer h-2 bg-slate-200 dark:bg-slate-800 rounded-lg"
          >
          <p class="text-[11px] text-slate-500 dark:text-slate-500">{{ t('settings.imageQualityHelp') }}</p>
        </div>
      </div>
    </div>

    <!-- AI Vision Recognition Settings (Backend) -->
    <div class="glass-card rounded-xl p-4 sm:p-6 space-y-5 shadow-xs">
      <div class="flex items-center gap-3 border-b border-slate-200 dark:border-slate-800/80 pb-3">
        <div class="p-2 bg-purple-500/10 text-purple-600 dark:text-purple-400 rounded-lg">
          <i data-lucide="sparkles" class="w-5 h-5"></i>
        </div>
        <div>
          <h3 class="font-bold text-sm sm:text-base text-slate-900 dark:text-white">
            {{ t('settings.aiSection') }}
          </h3>
          <p class="text-[11px] sm:text-xs text-slate-500 dark:text-slate-400">
            {{ t('settings.aiDesc') }}
          </p>
        </div>
      </div>

      <div class="space-y-4 text-xs sm:text-sm">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- AI Provider Selection -->
          <div class="space-y-1.5">
            <label class="block text-xs font-semibold text-slate-800 dark:text-slate-200">
              {{ t('settings.aiProvider') }}
            </label>
            <select
              v-model="localAiProvider"
              class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
            >
              <option value="gemini">Google Gemini Flash (Default)</option>
              <option value="openai">OpenAI (GPT-4o Vision)</option>
              <option value="anthropic">Anthropic Claude 3.5 Sonnet</option>
              <option value="ollama">Local Ollama / LLaVA Vision</option>
            </select>
            <p class="text-[11px] text-slate-500 dark:text-slate-500">{{ t('settings.aiProviderHelp') }}</p>
          </div>

          <!-- Auto-process toggle -->
          <div class="space-y-1.5 flex flex-col justify-between">
            <label class="block text-xs font-semibold text-slate-800 dark:text-slate-200">
              {{ t('settings.autoProcess') }}
            </label>
            <div class="flex items-center gap-3 pt-1">
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  v-model="localAutoProcess"
                  type="checkbox"
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-slate-300 peer-focus:outline-none rounded-full peer dark:bg-slate-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-slate-600 peer-checked:bg-emerald-600"></div>
                <span class="ml-2.5 text-xs text-slate-700 dark:text-slate-300 font-medium">
                  {{ localAutoProcess ? 'Enabled' : 'Disabled' }}
                </span>
              </label>
            </div>
            <p class="text-[11px] text-slate-500 dark:text-slate-500">{{ t('settings.autoProcessHelp') }}</p>
          </div>
        </div>

        <!-- Custom API Key input -->
        <div class="space-y-1.5">
          <label class="block text-xs font-semibold text-slate-800 dark:text-slate-200">
            {{ t('settings.customApiKey') }}
          </label>
          <div class="relative">
            <input
              v-model="localApiKey"
              :type="showApiKey ? 'text' : 'password'"
              :placeholder="t('settings.customApiKeyPlaceholder')"
              class="w-full pl-3 pr-10 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white font-mono text-xs focus:outline-none focus:border-emerald-500"
            >
            <button
              type="button"
              @click="showApiKey = !showApiKey"
              class="absolute right-3 top-2.5 text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 cursor-pointer"
            >
              <i :data-lucide="showApiKey ? 'eye-off' : 'eye'" class="w-4 h-4"></i>
            </button>
          </div>
          <p class="text-[11px] text-slate-500 dark:text-slate-500">{{ t('settings.customApiKeyHelp') }}</p>
        </div>
      </div>
    </div>

    <!-- System Info Banner -->
    <div class="bg-slate-100 dark:bg-slate-900/60 rounded-xl p-4 border border-slate-200 dark:border-slate-800/80 text-xs text-slate-600 dark:text-slate-400 flex flex-wrap items-center justify-between gap-3">
      <div class="flex items-center gap-2">
        <i data-lucide="server" class="w-4 h-4 text-emerald-600 dark:text-emerald-400"></i>
        <span><strong>{{ t('settings.serverStatus') }}:</strong> {{ t('settings.serverConnected') }} (Spring WebFlux + MongoDB GridFS)</span>
      </div>
      <div class="flex items-center gap-4 text-slate-500 dark:text-slate-400 font-mono text-[11px]">
        <span>{{ t('settings.backendVersion') }}: v1.0.0</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUiStore } from '@/stores/ui'
import { useSettingsStore } from '@/stores/settings'

const uiStore = useUiStore()
const settingsStore = useSettingsStore()
const { t, locale } = useI18n()

const showApiKey = ref(false)

const localLowStock = ref(settingsStore.lowStockThreshold)
const localImageQuality = ref(settingsStore.imageQuality)
const localDefaultPageSize = ref(settingsStore.defaultPageSize)
const localAutoProcess = ref(settingsStore.autoProcessAi)
const localAiProvider = ref(settingsStore.aiProvider)
const localApiKey = ref(settingsStore.customApiKey)

const syncLocalFromStore = () => {
  localLowStock.value = settingsStore.lowStockThreshold
  localImageQuality.value = settingsStore.imageQuality
  localDefaultPageSize.value = settingsStore.defaultPageSize
  localAutoProcess.value = settingsStore.autoProcessAi
  localAiProvider.value = settingsStore.aiProvider
  localApiKey.value = settingsStore.customApiKey
}

watch(
  () => [
    settingsStore.lowStockThreshold,
    settingsStore.imageQuality,
    settingsStore.defaultPageSize,
    settingsStore.autoProcessAi,
    settingsStore.aiProvider,
    settingsStore.customApiKey,
  ],
  () => {
    syncLocalFromStore()
  }
)

onMounted(() => {
  syncLocalFromStore()
})

const changeLocale = (lang: string) => {
  locale.value = lang
  localStorage.setItem('part_manager_locale', lang)
}

const saveSettings = async () => {
  await settingsStore.saveBackendSettings({
    lowStockThreshold: localLowStock.value,
    imageQuality: localImageQuality.value,
    defaultPageSize: localDefaultPageSize.value,
    autoProcessAi: localAutoProcess.value,
    aiProvider: localAiProvider.value,
    customApiKey: localApiKey.value,
  })
}

const resetDefaults = () => {
  localLowStock.value = 5
  localImageQuality.value = 80
  localDefaultPageSize.value = 20
  localAutoProcess.value = true
  localAiProvider.value = 'gemini'
  localApiKey.value = ''
  saveSettings()
}
</script>
