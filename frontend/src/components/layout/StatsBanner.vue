<template>
  <section class="bg-white/60 dark:bg-slate-900/50 border-b border-slate-200 dark:border-slate-800/80 px-3 sm:px-4 py-2 sm:py-2.5">
    <div class="max-w-7xl mx-auto flex flex-wrap items-center justify-between text-xs gap-2 sm:gap-3">
      <!-- Quick Counter Badges -->
      <div class="flex items-center gap-3 sm:gap-4 text-slate-500 dark:text-slate-400 font-mono text-[11px] sm:text-xs">
        <span class="flex items-center gap-1.5">
          <i data-lucide="layers" class="w-3.5 h-3.5 text-slate-400 dark:text-slate-500"></i>
          {{ t('stats.types') }} <strong class="text-slate-900 dark:text-white font-semibold">{{ catalogStore.components.length }}</strong>
        </span>
        <span class="flex items-center gap-1.5">
          <i data-lucide="boxes" class="w-3.5 h-3.5 text-slate-400 dark:text-slate-500"></i>
          {{ t('stats.total') }} <strong class="text-emerald-600 dark:text-emerald-400 font-semibold">{{ catalogStore.totalItemsCount }} {{ t('stats.pcs') }}</strong>
        </span>
        <span class="flex items-center gap-1.5">
          <i data-lucide="microchip" class="w-3.5 h-3.5 text-slate-400 dark:text-slate-500"></i>
          {{ t('stats.smd') }} <strong class="text-indigo-600 dark:text-indigo-400 font-semibold">{{ catalogStore.smdCount }}</strong>
        </span>
      </div>

      <!-- Quick View Filter Tabs -->
      <div
        v-if="catalogStore.dictionary.enabledAI"
        class="flex items-center space-x-1 bg-slate-200/70 dark:bg-slate-950/80 p-0.5 sm:p-1 rounded-lg border border-slate-300 dark:border-slate-800/80">
        <button
          @click="uiStore.currentView = 'catalog'" 
          class="px-2.5 py-1 rounded text-xs transition cursor-pointer"
          :class="uiStore.currentView === 'catalog' ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 font-medium shadow-xs' : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'"
        >
          {{ t('nav.catalog') }}
        </button>
        <button 
          @click="uiStore.currentView = 'queue'" 
          class="px-2.5 py-1 rounded text-xs transition flex items-center gap-1 cursor-pointer"
          :class="uiStore.currentView === 'queue' ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 font-medium shadow-xs' : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'"
        >
          <span>{{ t('nav.processing') }}</span>
          <span v-if="queueStore.pendingJobsCount" class="w-2 h-2 rounded-full bg-amber-500 dark:bg-amber-400 animate-ping"></span>
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useUiStore } from '../../stores/ui'
import { useCatalogStore } from '../../stores/catalog'
import { useQueueStore } from '../../stores/queue'

const uiStore = useUiStore()
const catalogStore = useCatalogStore()
const queueStore = useQueueStore()
const { t } = useI18n()
</script>
