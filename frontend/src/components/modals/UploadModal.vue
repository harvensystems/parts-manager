<template>
  <BaseModal
    v-model="uiStore.showUploadModal"
    :title="t('uploadModal.title')"
    :subtitle="t('uploadModal.subtitle')"
    icon="plus-circle"
    maxWidth="2xl"
    @close="handleClose"
  >
    <!-- Subheader Tabs -->
    <template #subheader>
      <div class="flex p-1 text-xs bg-slate-100 dark:bg-slate-900 rounded-lg">
        <button 
          @click="switchTab('manual')"
          class="flex-1 py-2 text-center rounded-lg font-medium transition cursor-pointer flex items-center justify-center gap-1.5"
          :class="uiStore.activeUploadTab === 'manual' ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'"
        >
          <i data-lucide="edit-3" class="w-3.5 h-3.5"></i>
          <span>{{ t('uploadModal.tabManual') }}</span>
        </button>
        <button
          v-if="catalogStore.dictionary.enabledAI"
          @click="switchTab('auto')"
          class="flex-1 py-2 text-center rounded-lg font-medium transition cursor-pointer flex items-center justify-center gap-1.5 relative"
          :class="uiStore.activeUploadTab === 'auto' ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'"
        >
          <i data-lucide="sparkles" class="w-3.5 h-3.5 text-amber-500"></i>
          <span class="font-semibold">{{ t('uploadModal.tabAuto') }}</span>
          <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
        </button>
      </div>
    </template>

    <!-- Modal Content -->
    <div class="space-y-4">
      <!-- 1. Manual Entry Form with Optional Photo Attachment -->
      <part-form v-if="uiStore.activeUploadTab === 'manual'" v-model="manualForm" />

      <!-- 2. Combined AI Recognition Tab: Live Camera + File Upload / Drag & Drop -->
      <div v-if="uiStore.activeUploadTab === 'auto'" class="space-y-3">
        <!-- Hidden file input for AI upload -->
        <input 
          ref="autoFileInputRef" 
          type="file" 
          accept="image/*" 
          class="hidden" 
          @change="handleAutoFileSelect"
        >

        <!-- Live Camera Stream container (when camera active) -->
        <div v-if="cameraActive || cameraLoading" class="relative bg-black rounded-xl overflow-hidden aspect-video sm:aspect-4/3 flex items-center justify-center border border-slate-300 dark:border-slate-800 shadow-inner">
          <video ref="videoRef" autoplay playsinline muted class="w-full h-full object-cover"></video>
          <canvas ref="canvasRef" class="hidden"></canvas>
          
          <div v-if="cameraLoading" class="absolute inset-0 flex items-center justify-center bg-slate-950/80">
            <i data-lucide="loader-2" class="w-8 h-8 text-emerald-400 animate-spin"></i>
          </div>

          <!-- Focus Target Overlay -->
          <div class="absolute inset-6 sm:inset-10 border-2 border-emerald-500/40 rounded-lg pointer-events-none flex items-center justify-center">
            <div class="text-[10px] sm:text-xs font-mono text-emerald-400 bg-slate-950/80 px-2 py-0.5 rounded">
              {{ t('uploadModal.focusOverlay') }}
            </div>
          </div>
        </div>

        <!-- Camera unavailable or disabled banner/dropzone -->
        <div 
          v-else
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleAutoDrop"
          @click="triggerAutoFileInput"
          class="border-2 border-dashed rounded-xl p-8 sm:p-10 text-center transition flex flex-col items-center justify-center cursor-pointer relative"
          :class="isDragging ? 'border-emerald-500 bg-emerald-500/5' : 'border-slate-300 dark:border-slate-700 hover:border-slate-400 dark:hover:border-slate-600 bg-slate-50/50 dark:bg-slate-900/50'"
        >
          <div class="w-14 h-14 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-emerald-600 dark:text-emerald-400 mb-3 shadow-md border border-slate-200 dark:border-slate-700">
            <i data-lucide="sparkles" class="w-7 h-7 text-amber-500"></i>
          </div>
          <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ t('uploadModal.uploadFileOption') }}</p>
          <p class="text-xs text-slate-500 dark:text-slate-400 mt-1">{{ t('uploadModal.cameraUnavailable') }}</p>
        </div>

        <!-- Primary Actions: Capture or Upload Ready Image -->
        <div class="flex flex-col sm:flex-row gap-2.5 pt-1">
          <button 
            v-if="cameraActive"
            @click="captureFrame"
            :disabled="cameraLoading"
            class="flex-1 py-3 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white dark:text-slate-950 font-bold text-sm rounded-xl transition flex items-center justify-center gap-2 shadow-lg shadow-emerald-950/50 cursor-pointer active:scale-98"
          >
            <i data-lucide="aperture" class="w-5 h-5"></i>
            {{ t('uploadModal.captureBtn') }}
          </button>

          <button 
            @click="triggerAutoFileInput"
            class="py-3 px-4 bg-slate-200 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 font-semibold text-sm rounded-xl transition flex items-center justify-center gap-2 cursor-pointer active:scale-98"
            :class="cameraActive ? 'sm:w-auto' : 'flex-1'"
          >
            <i data-lucide="upload" class="w-4 h-4"></i>
            {{ t('uploadModal.uploadFileOption') }}
          </button>

          <button 
            v-if="cameraActive"
            @click="stopCamera"
            class="py-3 px-3 text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200 text-xs rounded-xl transition cursor-pointer flex items-center justify-center gap-1"
            :title="t('uploadModal.stopCamera')"
          >
            <i data-lucide="video-off" class="w-4 h-4"></i>
            <span class="sm:hidden">{{ t('uploadModal.stopCamera') }}</span>
          </button>

          <button 
            v-else-if="!cameraActive && !cameraLoading"
            @click="initCamera"
            class="py-3 px-4 bg-emerald-600/15 hover:bg-emerald-600/25 text-emerald-700 dark:text-emerald-400 border border-emerald-500/30 font-semibold text-sm rounded-xl transition flex items-center justify-center gap-2 cursor-pointer active:scale-98"
          >
            <i data-lucide="camera" class="w-4 h-4"></i>
            {{ t('uploadModal.startCamera') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modal Footer -->
    <template #footer>
      <div v-if="uiStore.activeUploadTab === 'manual'" class="flex items-center justify-between gap-2">
        <button 
          type="button" 
          @click="handleClose" 
          class="px-4 py-2 bg-slate-200 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 font-medium text-xs sm:text-sm rounded-lg transition cursor-pointer"
        >
          {{ t('uploadModal.cancel') }}
        </button>

        <button 
          type="button" 
          @click="saveManual" 
          :disabled="isSavingManual"
          class="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white dark:text-slate-950 font-bold text-xs sm:text-sm rounded-lg shadow-lg shadow-emerald-950/60 transition flex items-center gap-1.5 cursor-pointer active:scale-95"
        >
          <i v-if="isSavingManual" data-lucide="loader-2" class="w-4 h-4 animate-spin"></i>
          <i v-else data-lucide="check" class="w-4 h-4"></i>
          {{ isSavingManual ? t('uploadModal.savingPart') : t('verifyModal.saveToCatalog') }}
        </button>
      </div>

      <div v-else class="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400">
        <span class="flex items-center gap-1.5">
          <i data-lucide="shield-check" class="w-4 h-4 text-emerald-600 dark:text-emerald-400"></i>
          <span class="hidden xs:inline">{{ t('uploadModal.nonBlocking') }}</span>
        </span>
        <button 
          @click="handleClose" 
          class="px-3.5 py-1.5 bg-slate-200 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 rounded-lg transition cursor-pointer"
        >
          {{ t('uploadModal.cancel') }}
        </button>
      </div>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, onUnmounted, nextTick, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUiStore } from '@/stores/ui'
import { useCatalogStore } from '@/stores/catalog'
import { useQueueStore } from '@/stores/queue'
import { api } from '@/api'
import BaseModal from '../common/BaseModal.vue'
import PartForm from "@/components/common/PartForm.vue";
import { CreateOrUpdatePartDto } from '@/api/api.ts'

const uiStore = useUiStore()
const catalogStore = useCatalogStore()
const queueStore = useQueueStore()
const { t } = useI18n()

// Tabs

const isDragging = ref(false)
const autoFileInputRef = ref<HTMLInputElement | null>(null)
const videoRef = ref<HTMLVideoElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const cameraLoading = ref(false)
const cameraActive = ref(false)
const isSavingManual = ref(false)
let mediaStream: MediaStream | null = null


const manualForm = ref<{ dto: CreateOrUpdatePartDto, file: File | null }>({
  file: null,
  dto: {} as CreateOrUpdatePartDto,
})

const saveManual = async () => {
  if (!manualForm.value.dto.name?.trim()) {
    uiStore.showToast(t('verifyModal.nameRequired'))
    return
  }

  isSavingManual.value = true
  try {
    let photoUrl = manualForm.value.dto.photoIds?.[0] ?? ''
    if (manualForm.value.file && (!manualForm.value.dto.photoIds || manualForm.value.dto.photoIds?.length === 0)) {
      photoUrl = (await api.images.uploadImage({ file: manualForm.value.file })).id ?? ''
      manualForm.value.dto.photoIds = [photoUrl]
    }

    await catalogStore.saveComponent(manualForm.value.dto)

    catalogStore.component = { } as CreateOrUpdatePartDto
    handleClose()
    uiStore.currentView = 'catalog'
  } finally {
    isSavingManual.value = false
  }
}

// Auto Tab Actions
const switchTab = async (tab: 'manual' | 'auto') => {
  uiStore.activeUploadTab = tab
  if (tab === 'auto') {
    await initCamera()
  } else {
    stopCamera()
  }
}

const triggerAutoFileInput = () => {
  autoFileInputRef.value?.click()
}

const handleAutoFileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files[0]) {
    processAutoFile(target.files[0])
  }
}

const handleAutoDrop = (e: DragEvent) => {
  isDragging.value = false
  if (e.dataTransfer?.files && e.dataTransfer.files[0]) {
    processAutoFile(e.dataTransfer.files[0])
  }
}

const processAutoFile = (file: File) => {
  queueStore.uploadFileToQueue(file)
  handleClose()
}

const initCamera = async () => {
  cameraLoading.value = true
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: {
        facingMode: 'environment'
      }, //{ facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } },
      audio: false,
    })
    await nextTick()
    if (videoRef.value) {
      videoRef.value.srcObject = mediaStream
      videoRef.value.play()
      cameraActive.value = true
    }
  } catch (err: any) {
    console.error('Camera access error:', err)
    cameraActive.value = false
  } finally {
    cameraLoading.value = false
  }
}

const captureFrame = () => {
  if (!videoRef.value || !canvasRef.value) return
  const video = videoRef.value
  const canvas = canvasRef.value
  canvas.width = video.videoWidth || 640
  canvas.height = video.videoHeight || 480
  const ctx = canvas.getContext('2d')
  if (ctx) {
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    const photoUrl = canvas.toDataURL('image/jpeg', 0.85)
    queueStore.addQueueJob(photoUrl)
    handleClose()
  }
}

const stopCamera = () => {
  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop())
    mediaStream = null
  }
  cameraActive.value = false
}

const handleClose = () => {
  stopCamera()
  uiStore.showUploadModal = false
}

watch(
  () => uiStore.showUploadModal,
  (show) => {
    manualForm.value = {
      dto: catalogStore.component,
      file: null,
    }
    if (!show) {
      stopCamera()
    } else if (uiStore.activeUploadTab === 'auto') {
      initCamera()
    }
  }
)

onUnmounted(() => {
  stopCamera()
})
</script>
