<template>
  <header class="sticky top-0 z-30 border-b border-slate-800 bg-slate-900/95 backdrop-blur-md px-3 sm:px-4 py-2.5 sm:py-3">
    <div class="max-w-7xl mx-auto flex items-center justify-between">
      <!-- App Brand Logo -->
      <div class="flex items-center space-x-2.5 sm:space-x-3">
        <div class="p-1.5 sm:p-2 bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded-lg shadow-inner">
          <i data-lucide="cpu" class="w-5 h-5 sm:w-6 sm:h-6"></i>
        </div>
        <div>
          <h1 class="font-bold text-base sm:text-lg tracking-tight text-white flex items-center gap-1.5 sm:gap-2">
            {{ t('app.title') }}
          </h1>
          <p class="text-[11px] text-slate-400 hidden md:block">{{ t('app.subtitle') }}</p>
        </div>
      </div>

      <!-- Header Action Buttons -->
      <div class="flex items-center space-x-1.5 sm:space-x-3">
        <!-- Language Switcher -->
        <div class="flex items-center bg-slate-950/80 p-0.5 rounded-lg border border-slate-800 text-xs">
          <button 
            type="button"
            @click="switchLocale('ua')" 
            class="px-2 py-1 rounded transition cursor-pointer font-semibold text-[11px]"
            :class="locale === 'ua' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40' : 'text-slate-400 hover:text-slate-200'"
          >
            UA
          </button>
          <button 
            type="button"
            @click="switchLocale('en')" 
            class="px-2 py-1 rounded transition cursor-pointer font-semibold text-[11px]"
            :class="locale === 'en' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40' : 'text-slate-400 hover:text-slate-200'"
          >
            EN
          </button>
        </div>

        <!-- Direct Manual Add Button -->
        <button 
          @click="store.startManualEntry"
          class="px-2.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 font-medium text-xs rounded-lg transition flex items-center gap-1.5 cursor-pointer"
          :title="t('app.manualTooltip')"
        >
          <i data-lucide="edit-3" class="w-3.5 h-3.5 text-emerald-400"></i>
          <span class="hidden sm:inline">{{ t('app.manual') }}</span>
        </button>

        <!-- Main Photo Upload CTA -->
        <button 
          @click="store.showUploadModal = true"
          class="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-slate-950 font-semibold text-xs sm:text-sm rounded-lg shadow-md hover:shadow-emerald-900/40 transition flex items-center gap-1.5 cursor-pointer active:scale-95"
        >
          <i data-lucide="camera" class="w-4 h-4"></i>
          <span class="hidden sm:inline">{{ t('app.addPart') }}</span>
          <span class="sm:hidden">{{ t('app.photo') }}</span>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useInventoryStore } from '../../stores/inventory'

const store = useInventoryStore()
const { t, locale } = useI18n()

const switchLocale = (lang: string) => {
  locale.value = lang
  localStorage.setItem('part_manager_locale', lang)
}
</script>
