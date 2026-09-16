<template>
  <nav class="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-white/95 dark:bg-slate-900/95 backdrop-blur-lg border-t border-slate-200 dark:border-slate-800 px-4 py-2 flex items-center justify-around text-xs shadow-2xl safe-area-pb">
    <button 
      @click="store.currentView = 'catalog'"
      class="flex flex-col items-center gap-1 transition p-1 cursor-pointer"
      :class="store.currentView === 'catalog' ? 'text-emerald-600 dark:text-emerald-400 font-semibold' : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'"
    >
      <i data-lucide="layers" class="w-5 h-5"></i>
      <span>{{ t('nav.catalog') }}</span>
    </button>

    <button 
      @click="store.showUploadModal = true"
      class="flex flex-col items-center -mt-5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 p-3 rounded-full shadow-lg shadow-emerald-900/50 border-4 border-slate-100 dark:border-slate-950 transition active:scale-95 cursor-pointer"
    >
      <i data-lucide="camera" class="w-6 h-6"></i>
    </button>

    <button 
      @click="store.currentView = 'queue'"
      class="flex flex-col items-center gap-1 relative transition p-1 cursor-pointer"
      :class="store.currentView === 'queue' ? 'text-emerald-600 dark:text-emerald-400 font-semibold' : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'"
    >
      <i data-lucide="sparkles" class="w-5 h-5"></i>
      <span>{{ t('nav.queue') }}</span>
      <span 
        v-if="store.pendingJobsCount" 
        class="absolute -top-1 right-2 px-1.5 py-0.2 bg-amber-500 text-slate-950 font-bold text-[9px] rounded-full animate-bounce"
      >
        {{ store.pendingJobsCount }}
      </span>
    </button>

<!--    <button -->
<!--      @click="store.currentView = 'settings'"-->
<!--      class="flex flex-col items-center gap-1 transition p-1 cursor-pointer"-->
<!--      :class="store.currentView === 'settings' ? 'text-emerald-600 dark:text-emerald-400 font-semibold' : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'"-->
<!--    >-->
<!--      <i data-lucide="settings" class="w-5 h-5"></i>-->
<!--      <span>{{ t('nav.settings') }}</span>-->
<!--    </button>-->
  </nav>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useInventoryStore } from '../../stores/inventory'

const store = useInventoryStore()
const { t } = useI18n()
</script>

<style scoped>
.safe-area-pb {
  padding-bottom: env(safe-area-inset-bottom, 0.5rem);
}
</style>
