<template>
  <header class="sticky top-0 z-30 border-b border-slate-200 dark:border-slate-800 bg-white/90 dark:bg-slate-900/95 backdrop-blur-md px-3 sm:px-4 py-2.5 sm:py-3 shadow-xs">
    <div class="max-w-7xl mx-auto flex items-center justify-between">
      <!-- App Brand Logo (Clickable to return to Catalog) -->
      <div 
        @click="store.currentView = 'catalog'"
        class="flex items-center space-x-2.5 sm:space-x-3 cursor-pointer group"
      >
        <div class="p-1.5 sm:p-2 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 rounded-lg shadow-inner group-hover:scale-105 transition">
          <i data-lucide="cpu" class="w-5 h-5 sm:w-6 sm:h-6"></i>
        </div>
        <div>
          <h1 class="font-bold text-base sm:text-lg tracking-tight text-slate-900 dark:text-white flex items-center gap-1.5 sm:gap-2">
            {{ t('app.title') }}
          </h1>
          <p class="text-[11px] text-slate-500 dark:text-slate-400 hidden md:block">{{ t('app.subtitle') }}</p>
        </div>
      </div>

      <!-- Header Action Buttons -->
      <div class="flex items-center space-x-1.5 sm:space-x-2.5">

        <!-- Main Photo Upload CTA -->
        <button 
          @click="store.showUploadModal = true"
          class="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white dark:text-slate-950 font-semibold text-xs sm:text-sm rounded-lg shadow-md hover:shadow-emerald-900/40 transition flex items-center gap-1.5 cursor-pointer active:scale-95"
        >
          <i data-lucide="camera" class="w-4 h-4"></i>
          <span class="hidden sm:inline">{{ t('app.addPart') }}</span>
          <span class="sm:hidden">{{ t('app.photo') }}</span>
        </button>

        <!-- Settings Button -->
        <button
            type="button"
            @click="store.currentView = 'settings'"
            class="p-2 text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white rounded-lg border border-slate-300 dark:border-slate-700/80 transition cursor-pointer flex items-center gap-1.5 text-xs font-medium"
            :class="store.currentView === 'settings' ? 'bg-emerald-500/15 border-emerald-500 text-emerald-600 dark:text-emerald-400 font-semibold' : 'bg-slate-50 dark:bg-slate-800/80'"
            :title="t('nav.settings')"
        >
          <i data-lucide="settings" class="w-4 h-4"></i>
          <span class="hidden">{{ t('nav.settings') }}</span>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useInventoryStore } from '../../stores/inventory'

const store = useInventoryStore()
const { t } = useI18n()
</script>
