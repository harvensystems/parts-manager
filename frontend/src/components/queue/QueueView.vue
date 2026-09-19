<template>
  <div class="space-y-4 sm:space-y-6">
    <!-- AI Disabled Notice -->
    <div v-if="catalogStore.dictionary.enabledAI === false" class="glass-card rounded-2xl p-8 sm:p-12 text-center max-w-lg mx-auto my-6 sm:my-8 shadow-xs border border-amber-300 dark:border-amber-800/60">
      <div class="w-14 h-14 sm:w-16 sm:h-16 bg-amber-500/10 rounded-full flex items-center justify-center mx-auto text-amber-500 dark:text-amber-400 border border-amber-500/30 mb-3 sm:mb-4">
        <i data-lucide="key-round" class="w-7 h-7 sm:w-8 sm:h-8"></i>
      </div>
      <h3 class="text-base sm:text-lg font-bold text-slate-900 dark:text-white">{{ t('queue.aiDisabledTitle') }}</h3>
      <p class="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-2 leading-relaxed">
        {{ t('queue.aiDisabledText') }}
      </p>
      <button 
        @click="uiStore.currentView = 'settings'" 
        class="mt-5 px-4 py-2 bg-amber-500 hover:bg-amber-600 text-slate-950 font-semibold text-xs sm:text-sm rounded-lg shadow inline-flex items-center gap-2 transition cursor-pointer"
      >
        <i data-lucide="settings" class="w-4 h-4"></i>
        {{ t('queue.goToSettings') }}
      </button>
    </div>

    <!-- AI Enabled: Active Tasks & Content -->
    <template v-else>
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
          @click="uploadPhotoHandle"
          class="self-start sm:self-auto px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white dark:text-slate-950 text-xs sm:text-sm font-semibold rounded-lg flex items-center gap-1.5 transition cursor-pointer"
        >
          <i data-lucide="plus" class="w-4 h-4"></i>
          {{ t('queue.uploadAnother') }}
        </button>
      </div>

      <!-- AI Queue Items (Table on Desktop, Cards on Mobile) -->
      <div v-if="queueStore.aiQueue.length > 0" class="glass-card rounded-xl overflow-hidden border border-slate-200 dark:border-slate-800 shadow-xs">
        
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
              <tr v-for="job in queueStore.aiQueue" :key="job.id" class="hover:bg-slate-100/60 dark:hover:bg-slate-900/40 transition">
                <!-- Thumbnail -->
                <td class="p-3.5">
                  <div class="w-12 h-12 bg-slate-100 dark:bg-slate-900 rounded border border-slate-200 dark:border-slate-800 overflow-hidden relative">
                    <img :src="catalogStore.getImageUrl(job.photoId)" class="w-full h-full object-cover">
                    <div v-if="job.status === 'PROCESSING'" class="scan-line"></div>
                  </div>
                </td>

                <!-- Date -->
                <td class="p-3.5 font-mono text-slate-500 dark:text-slate-400">
                  {{ formatTime(job.createdAt!) }}
                </td>

                <!-- Status Badge -->
                <td class="p-3.5">
                  <span 
                    class="px-2 py-1 rounded-full text-[10px] font-semibold flex items-center gap-1.5 w-max"
                    :class="{
                      'bg-amber-500/10 text-amber-600 dark:text-amber-300 border border-amber-500/30': job.status === 'PROCESSING',
                      'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30': job.status === 'COMPLETED',
                      'bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/30': job.status === 'FAILED',
                      'bg-slate-500/10 text-slate-600 dark:text-slate-400 border border-slate-500/30': job.status === 'PENDING'
                    }"
                  >
                    <span 
                      class="w-1.5 h-1.5 rounded-full"
                      :class="{
                        'bg-amber-500 animate-pulse': job.status === 'PROCESSING',
                        'bg-emerald-500': job.status === 'COMPLETED',
                        'bg-rose-500': job.status === 'FAILED',
                        'bg-slate-400': job.status === 'PENDING'
                      }"
                    ></span>
                    {{ getStatusLabel(job.status!) }}
                  </span>
                </td>

                <!-- Recognized Info Preview -->
                <td class="p-3.5">
                  <div v-if="job.status === 'PROCESSING'" class="flex items-center gap-2 text-slate-500 dark:text-slate-400 italic">
                    <i data-lucide="loader-2" class="w-3.5 h-3.5 animate-spin text-amber-500"></i>
                    {{ t('queue.processingText') }}
                  </div>
                  <div v-else-if="job.status === 'COMPLETED' && job.aiResult" class="space-y-0.5">
                    <div class="font-bold text-slate-900 dark:text-white flex items-center gap-2">
                      {{ job.aiResult.name }}
                      <span v-if="job.aiResult.confidence" class="px-1.5 py-0.2 text-[9px] rounded bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 font-mono">
                        {{ job.aiResult.confidence }}% {{ t('queue.confidence') }}
                      </span>
                    </div>
                    <div class="text-[11px] text-slate-500 dark:text-slate-400 flex items-center gap-2">
                      <span>{{ job.aiResult.type }}</span>
                      <span>•</span>
                      <span>{{ job.aiResult.package || 'N/A' }}</span>
                      <span>•</span>
                      <span class="text-emerald-600 dark:text-emerald-400 font-mono font-medium">{{ job.aiResult.quantity || 1 }} {{ t('queue.pcs') }}</span>
                    </div>
                  </div>
                  <div v-else-if="job.status === 'FAILED'" class="text-rose-600 dark:text-rose-400 text-xs flex items-center gap-1.5">
                    <i data-lucide="alert-circle" class="w-3.5 h-3.5"></i>
                    {{ t('queue.failedText') }}
                  </div>
                </td>

                <!-- Action Buttons -->
                <td class="p-3.5 text-right space-x-1.5">
                  <button 
                    v-if="job.status === 'COMPLETED'"
                    @click="queueStore.openVerifyModalForJob(job)"
                    class="px-2.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white dark:text-slate-950 text-xs font-semibold rounded-md shadow inline-flex items-center gap-1 transition cursor-pointer"
                  >
                    <i data-lucide="check" class="w-3.5 h-3.5"></i>
                    {{ t('queue.verifyAndSave') }}
                  </button>
                  <button 
                    v-if="job.status === 'FAILED'"
                    @click="queueStore.retryAiJob(job)"
                    class="px-2.5 py-1.5 bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 text-xs font-medium rounded-md border border-slate-300 dark:border-slate-700 inline-flex items-center gap-1 transition cursor-pointer"
                  >
                    <i data-lucide="rotate-cw" class="w-3.5 h-3.5"></i>
                    {{ t('queue.retry') }}
                  </button>
                  <button 
                    @click="queueStore.removeQueueJob(job.id!)"
                    class="p-1.5 hover:bg-rose-500/10 text-slate-400 hover:text-rose-600 dark:hover:text-rose-400 rounded-md transition cursor-pointer"
                    :title="t('queue.delete')"
                  >
                    <i data-lucide="trash-2" class="w-4 h-4"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Mobile Card List View (Visible only on mobile) -->
        <div class="md:hidden divide-y divide-slate-200 dark:divide-slate-800/80">
          <div 
            v-for="job in queueStore.aiQueue" 
            :key="job.id" 
            class="p-3.5 space-y-3"
          >
            <div class="flex items-center justify-between gap-2">
              <span 
                class="px-2 py-0.5 rounded-full text-[10px] font-semibold flex items-center gap-1.5"
                :class="{
                  'bg-amber-500/10 text-amber-600 dark:text-amber-300 border border-amber-500/30': job.status === 'PROCESSING',
                  'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30': job.status === 'COMPLETED',
                  'bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/30': job.status === 'FAILED',
                  'bg-slate-500/10 text-slate-600 dark:text-slate-400 border border-slate-500/30': job.status === 'PENDING'
                }"
              >
                <span 
                  class="w-1.5 h-1.5 rounded-full"
                  :class="{
                    'bg-amber-500 animate-pulse': job.status === 'PROCESSING',
                    'bg-emerald-500': job.status === 'COMPLETED',
                    'bg-rose-500': job.status === 'FAILED',
                    'bg-slate-400': job.status === 'PENDING'
                  }"
                ></span>
                {{ getStatusLabel(job.status!) }}
              </span>
              <span class="text-[10px] font-mono text-slate-400 dark:text-slate-500">{{ formatTime(job.createdAt!) }}</span>
            </div>

            <div class="flex gap-3">
              <div class="w-16 h-16 bg-slate-100 dark:bg-slate-900 rounded border border-slate-200 dark:border-slate-800 overflow-hidden relative shrink-0">
                <img :src="catalogStore.getImageUrl(job.photoId)" class="w-full h-full object-cover">
                <div v-if="job.status === 'PROCESSING'" class="scan-line"></div>
              </div>

              <div class="flex-1 min-w-0">
                <div v-if="job.status === 'PROCESSING'" class="text-xs text-slate-500 dark:text-slate-400 italic flex items-center gap-1.5 h-full">
                  <i data-lucide="loader-2" class="w-3.5 h-3.5 animate-spin text-amber-500"></i>
                  {{ t('queue.processingText') }}
                </div>
                <div v-else-if="job.status === 'COMPLETED' && job.aiResult" class="space-y-1">
                  <h4 class="font-bold text-xs text-slate-900 dark:text-white truncate">{{ job.aiResult.name }}</h4>
                  <div class="text-[11px] text-slate-500 dark:text-slate-400 flex flex-wrap gap-1">
                    <span>{{ job.aiResult.type }}</span>
                    <span>•</span>
                    <span>{{ job.aiResult.package || 'N/A' }}</span>
                  </div>
                  <div class="text-[11px] font-mono font-semibold text-emerald-600 dark:text-emerald-400">
                    {{ job.aiResult.quantity || 1 }} {{ t('queue.pcs') }}
                  </div>
                </div>
                <div v-else-if="job.status === 'FAILED'" class="text-rose-600 dark:text-rose-400 text-xs">
                  {{ t('queue.failedText') }}
                </div>
              </div>
            </div>

            <!-- Mobile Actions -->
            <div class="pt-2 flex items-center justify-end gap-2 border-t border-slate-200 dark:border-slate-800/60">
              <button 
                @click="queueStore.removeQueueJob(job.id!)"
                class="p-1.5 text-slate-400 hover:text-rose-600 dark:hover:text-rose-400 rounded cursor-pointer"
              >
                <i data-lucide="trash-2" class="w-4 h-4"></i>
              </button>
              <button 
                v-if="job.status === 'COMPLETED'"
                @click="queueStore.openVerifyModalForJob(job)"
                class="px-3 py-1 bg-emerald-600 hover:bg-emerald-500 text-white dark:text-slate-950 text-xs font-semibold rounded shadow flex items-center gap-1 cursor-pointer"
              >
                <i data-lucide="check" class="w-3.5 h-3.5"></i>
                {{ t('queue.verifyAndSave') }}
              </button>
              <button 
                v-if="job.status === 'FAILED'"
                @click="queueStore.retryAiJob(job)"
                class="px-3 py-1 bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 text-xs rounded border border-slate-300 dark:border-slate-700 flex items-center gap-1 cursor-pointer"
              >
                <i data-lucide="rotate-cw" class="w-3.5 h-3.5"></i>
                {{ t('queue.retry') }}
              </button>
            </div>
          </div>
        </div>

      </div>

      <!-- Empty State -->
      <div v-else class="glass-card rounded-2xl p-8 sm:p-12 text-center max-w-md mx-auto my-6 sm:my-8 shadow-xs">
        <div class="w-14 h-14 sm:w-16 sm:h-16 bg-slate-100 dark:bg-slate-900 rounded-full flex items-center justify-center mx-auto text-slate-400 dark:text-slate-500 border border-slate-300 dark:border-slate-800 mb-3 sm:mb-4">
          <i data-lucide="sparkles" class="w-7 h-7 sm:w-8 sm:h-8 text-amber-500 dark:text-amber-400"></i>
        </div>
        <h3 class="text-base sm:text-lg font-semibold text-slate-900 dark:text-white">{{ t('queue.emptyTitle') }}</h3>
        <p class="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
          {{ t('queue.emptyText') }}
        </p>
        <button 
          @click="uiStore.showUploadModal = true" 
          class="mt-4 sm:mt-5 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white dark:text-slate-950 font-semibold text-xs sm:text-sm rounded-lg shadow inline-flex items-center gap-2 transition cursor-pointer"
        >
          <i data-lucide="camera" class="w-4 h-4"></i>
          {{ t('queue.uploadAnother') }}
        </button>
      </div>
    </template>

  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useUiStore } from '@/stores/ui'
import { useQueueStore } from '@/stores/queue'
import { useCatalogStore } from '@/stores'

const uiStore = useUiStore()
const queueStore = useQueueStore()
const catalogStore = useCatalogStore()
const { t } = useI18n()

const formatTime = (ts: number) => {
  return new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING: t('queue.statusPending'),
    PROCESSING: t('queue.statusProcessing'),
    COMPLETE: t('queue.statusCompleted'),
    FAILED: t('queue.statusFailed'),
  }
  return map[status] || status
}

const uploadPhotoHandle = () => {
  uiStore.activeUploadTab = 'auto'
  uiStore.showUploadModal = true
}

</script>

<style scoped>
.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #10b981, transparent);
  box-shadow: 0 0 8px #10b981;
  animation: scan 1.5s infinite ease-in-out;
}

@keyframes scan {
  0% {
    top: 0%;
  }
  50% {
    top: 100%;
  }
  100% {
    top: 0%;
  }
}
</style>
