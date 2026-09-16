<template>
  <div class="space-y-4 sm:space-y-6">
    <!-- Queue Header Bar -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
      <div>
        <h2 class="text-lg sm:text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
          <i data-lucide="sparkles" class="w-5 h-5 text-amber-500 dark:text-amber-400"></i>
          {{ t('queue.title') }}
        </h2>
        <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
          {{ t('queue.subtitle') }}
        </p>
      </div>

      <button 
        @click="store.showUploadModal = true"
        class="self-start sm:self-auto px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white dark:text-slate-950 text-xs sm:text-sm font-semibold rounded-lg flex items-center gap-1.5 transition cursor-pointer"
      >
        <i data-lucide="plus" class="w-4 h-4"></i>
        {{ t('queue.uploadAnother') }}
      </button>
    </div>

    <!-- AI Queue Items (Table on Desktop, Cards on Mobile) -->
    <div v-if="store.aiQueue.length > 0" class="glass-card rounded-xl overflow-hidden border border-slate-200 dark:border-slate-800 shadow-xs">
      
      <!-- Desktop Table View (Hidden on mobile) -->
      <div class="hidden md:block overflow-x-auto">
        <table class="w-full text-left text-xs text-slate-700 dark:text-slate-300">
          <thead class="bg-slate-100/90 dark:bg-slate-900/80 uppercase text-[10px] tracking-wider text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800 font-mono">
            <tr>
              <th class="p-3.5">{{ t('queue.preview') }}</th>
              <th class="p-3.5">{{ t('queue.createdAt') }}</th>
              <th class="p-3.5">{{ t('queue.status') }}</th>
              <th class="p-3.5">{{ t('queue.recognizedComponent') }}</th>
              <th class="p-3.5 text-right">{{ t('queue.actions') }}</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-200 dark:divide-slate-800/80">
            <tr v-for="job in store.aiQueue" :key="job.id" class="hover:bg-slate-100/60 dark:hover:bg-slate-900/40 transition">
              <!-- Thumbnail -->
              <td class="p-3.5">
                <div class="w-12 h-12 bg-slate-100 dark:bg-slate-900 rounded border border-slate-200 dark:border-slate-800 overflow-hidden relative">
                  <img :src="job.photoUrl" class="w-full h-full object-cover">
                  <div v-if="job.status === 'processing'" class="scan-line"></div>
                </div>
              </td>

              <!-- Date -->
              <td class="p-3.5 font-mono text-slate-500 dark:text-slate-400">
                {{ formatTime(job.createdAt) }}
              </td>

              <!-- Status Badge -->
              <td class="p-3.5">
                <span 
                  class="px-2 py-1 rounded-full text-[10px] font-semibold flex items-center gap-1.5 w-max"
                  :class="{
                    'bg-amber-500/10 text-amber-600 dark:text-amber-300 border border-amber-500/30': job.status === 'processing',
                    'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30': job.status === 'completed',
                    'bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/30': job.status === 'failed',
                    'bg-slate-200 dark:bg-slate-800 text-slate-600 dark:text-slate-400': job.status === 'pending'
                  }"
                >
                  <i v-if="job.status === 'processing'" data-lucide="loader-2" class="w-3 h-3 animate-spin text-amber-500 dark:text-amber-400"></i>
                  <i v-if="job.status === 'completed'" data-lucide="check-circle-2" class="w-3 h-3 text-emerald-600 dark:text-emerald-400"></i>
                  <i v-if="job.status === 'failed'" data-lucide="alert-triangle" class="w-3 h-3 text-rose-600 dark:text-rose-400"></i>
                  {{ getStatusLabel(job.status) }}
                </span>
                <div v-if="job.error" class="text-[10px] text-rose-500 dark:text-rose-400 mt-1 max-w-xs truncate">
                  {{ job.error }}
                </div>
              </td>

              <!-- Recognized Info -->
              <td class="p-3.5">
                <div v-if="job.aiResult">
                  <div class="font-bold text-slate-900 dark:text-white text-sm">{{ job.aiResult.name || t('queue.recognized') }}</div>
                  <div class="text-[11px] text-slate-500 dark:text-slate-400 flex items-center gap-2 font-mono mt-0.5">
                    <span>{{ t(`types.${job.aiResult.type}`) }}</span>
                    <span v-if="job.aiResult.partNumber">• {{ job.aiResult.partNumber }}</span>
                    <span v-if="job.aiResult.confidence" class="text-emerald-600 dark:text-emerald-400 font-semibold">• {{ job.aiResult.confidence }}% {{ t('queue.confidence') }}</span>
                  </div>
                </div>
                <div v-else class="text-slate-400 dark:text-slate-500 italic flex items-center gap-1.5">
                  <i data-lucide="loader-2" class="w-3.5 h-3.5 animate-spin text-amber-500 dark:text-amber-400"></i>
                  {{ t('queue.processingText') }}
                </div>
              </td>

              <!-- Action Buttons -->
              <td class="p-3.5 text-right">
                <div class="flex items-center justify-end space-x-2">
                  <button 
                    v-if="job.status === 'completed'"
                    @click="store.openVerifyModalForJob(job)"
                    class="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white dark:text-slate-950 font-bold rounded text-xs transition flex items-center gap-1 shadow cursor-pointer active:scale-95"
                  >
                    <i data-lucide="check" class="w-3.5 h-3.5"></i>
                    {{ t('queue.verifyAndSave') }}
                  </button>
                  <button 
                    v-if="job.status === 'failed' || job.status === 'pending'"
                    @click="store.retryAiJob(job)"
                    class="px-2.5 py-1.5 bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 rounded text-xs border border-slate-300 dark:border-slate-700 transition cursor-pointer"
                  >
                    {{ t('queue.retry') }}
                  </button>
                  <button 
                    @click="store.removeQueueJob(job.id)"
                    class="p-1.5 text-slate-400 hover:text-rose-500 dark:text-slate-500 dark:hover:text-rose-400 rounded transition cursor-pointer"
                    :title="t('queue.delete')"
                  >
                    <i data-lucide="trash-2" class="w-4 h-4"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile Cards View (Shown only on small screens) -->
      <div class="md:hidden divide-y divide-slate-200 dark:divide-slate-800">
        <div 
          v-for="job in store.aiQueue" 
          :key="job.id" 
          class="p-3.5 space-y-3"
        >
          <div class="flex items-start gap-3">
            <div class="w-14 h-14 bg-slate-100 dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 overflow-hidden relative shrink-0">
              <img :src="job.photoUrl" class="w-full h-full object-cover">
              <div v-if="job.status === 'processing'" class="scan-line"></div>
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between gap-2">
                <span 
                  class="px-2 py-0.5 rounded text-[10px] font-semibold flex items-center gap-1"
                  :class="{
                    'bg-amber-500/10 text-amber-600 dark:text-amber-300 border border-amber-500/30': job.status === 'processing',
                    'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30': job.status === 'completed',
                    'bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/30': job.status === 'failed',
                    'bg-slate-200 dark:bg-slate-800 text-slate-600 dark:text-slate-400': job.status === 'pending'
                  }"
                >
                  <i v-if="job.status === 'processing'" data-lucide="loader-2" class="w-3 h-3 animate-spin"></i>
                  {{ getStatusLabel(job.status) }}
                </span>
                <span class="text-[10px] font-mono text-slate-400 dark:text-slate-500">{{ formatTime(job.createdAt) }}</span>
              </div>

              <div class="mt-1">
                <div v-if="job.aiResult" class="font-bold text-slate-900 dark:text-white text-xs truncate">
                  {{ job.aiResult.name || t('queue.recognized') }}
                </div>
                <div v-else class="text-[11px] text-slate-400 dark:text-slate-500 italic">
                  {{ t('queue.processingText') }}
                </div>
              </div>
            </div>
          </div>

          <!-- Mobile Action Row -->
          <div class="flex items-center justify-between gap-2 pt-1">
            <button 
              @click="store.removeQueueJob(job.id)"
              class="text-xs text-rose-600 dark:text-rose-400 hover:underline p-1 cursor-pointer"
            >
              {{ t('queue.delete') }}
            </button>

            <button 
              v-if="job.status === 'completed'"
              @click="store.openVerifyModalForJob(job)"
              class="px-3 py-1.5 bg-emerald-600 text-white dark:text-slate-950 font-bold rounded-lg text-xs flex items-center gap-1 cursor-pointer active:scale-95"
            >
              <i data-lucide="check" class="w-3.5 h-3.5"></i>
              {{ t('queue.verifyAndSave') }}
            </button>

            <button 
              v-if="job.status === 'failed' || job.status === 'pending'"
              @click="store.retryAiJob(job)"
              class="px-3 py-1.5 bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-200 rounded-lg text-xs border border-slate-300 dark:border-slate-700 cursor-pointer"
            >
              {{ t('queue.retry') }}
            </button>
          </div>
        </div>
      </div>

    </div>

    <!-- Empty Queue State -->
    <div v-else class="glass-card rounded-2xl p-8 sm:p-12 text-center max-w-md mx-auto my-6 sm:my-8 shadow-xs">
      <div class="w-14 h-14 sm:w-16 sm:h-16 bg-slate-100 dark:bg-slate-900 rounded-full flex items-center justify-center mx-auto text-emerald-600 dark:text-emerald-400/80 border border-slate-300 dark:border-slate-800 mb-3 sm:mb-4">
        <i data-lucide="check-circle" class="w-7 h-7 sm:w-8 sm:h-8"></i>
      </div>
      <h3 class="text-base sm:text-lg font-semibold text-slate-900 dark:text-white">{{ t('queue.emptyTitle') }}</h3>
      <p class="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
        {{ t('queue.emptyText') }}
      </p>
      <button 
        @click="store.showUploadModal = true" 
        class="mt-4 sm:mt-5 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white dark:text-slate-950 font-semibold text-xs sm:text-sm rounded-lg shadow inline-flex items-center gap-2 transition cursor-pointer"
      >
        <i data-lucide="camera" class="w-4 h-4"></i>
        {{ t('queue.addNew') }}
      </button>
    </div>

  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useInventoryStore } from '../../stores/inventory'
import type { QueueJobStatus } from '../../types/inventory'

const store = useInventoryStore()
const { t } = useI18n()

const formatTime = (ts: number) => {
  const d = new Date(ts)
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

const getStatusLabel = (status: QueueJobStatus) => {
  switch (status) {
    case 'processing': return t('queue.statusProcessing')
    case 'completed': return t('queue.statusCompleted')
    case 'failed': return t('queue.statusFailed')
    case 'pending': return t('queue.statusPending')
    default: return status
  }
}
</script>
