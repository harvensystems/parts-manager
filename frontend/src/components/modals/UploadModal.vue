<template>
  <BaseModal
    v-model="store.showUploadModal"
    :title="t('uploadModal.title')"
    :subtitle="t('uploadModal.subtitle')"
    icon="camera"
    maxWidth="lg"
    @close="handleClose"
  >
    <!-- Subheader Tabs -->
    <template #subheader>
      <div class="flex p-1 text-xs">
        <button 
          @click="switchTab('file')"
          class="flex-1 py-2 text-center rounded-lg font-medium transition cursor-pointer"
          :class="uploadTab === 'file' ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'"
        >
          {{ t('uploadModal.tabFile') }}
        </button>
        <button 
          @click="switchTab('stream')"
          class="flex-1 py-2 text-center rounded-lg font-medium transition cursor-pointer flex items-center justify-center gap-1.5"
          :class="uploadTab === 'stream' ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'"
        >
          <span>{{ t('uploadModal.tabCamera') }}</span>
          <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
        </button>
        <button 
          @click="switchTab('samples')"
          class="flex-1 py-2 text-center rounded-lg font-medium transition cursor-pointer"
          :class="uploadTab === 'samples' ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'"
        >
          {{ t('uploadModal.tabSamples') }}
        </button>
      </div>
    </template>

    <!-- Modal Content -->
    <div class="space-y-4">
      <!-- 1. File Upload / Drag & Drop -->
      <div v-if="uploadTab === 'file'" class="space-y-3">
        <div 
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleDrop"
          class="border-2 border-dashed rounded-xl p-8 sm:p-10 text-center transition flex flex-col items-center justify-center cursor-pointer relative"
          :class="isDragging ? 'border-emerald-500 bg-emerald-500/5' : 'border-slate-300 dark:border-slate-700 hover:border-slate-400 dark:hover:border-slate-600 bg-slate-50/50 dark:bg-slate-900/50'"
          @click="triggerFileInput"
        >
          <input 
            ref="fileInputRef" 
            type="file" 
            accept="image/*" 
            capture="environment"
            class="hidden" 
            @change="handleFileSelect"
          >
          <div class="w-14 h-14 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-emerald-600 dark:text-emerald-400 mb-3 shadow-md border border-slate-200 dark:border-slate-700">
            <i data-lucide="upload-cloud" class="w-7 h-7"></i>
          </div>
          <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ t('uploadModal.dropzoneTitle') }}</p>
          <p class="text-xs text-slate-500 dark:text-slate-400 mt-1">{{ t('uploadModal.dropzoneSubtitle') }}</p>
        </div>
      </div>

      <!-- 2. Live Camera Stream -->
      <div v-if="uploadTab === 'stream'" class="space-y-3">
        <div class="relative bg-black rounded-xl overflow-hidden aspect-video sm:aspect-4/3 flex items-center justify-center border border-slate-300 dark:border-slate-800">
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

        <button 
          @click="captureFrame"
          :disabled="cameraLoading"
          class="w-full py-3 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white dark:text-slate-950 font-bold text-sm rounded-xl transition flex items-center justify-center gap-2 shadow-lg shadow-emerald-950/50 cursor-pointer active:scale-98"
        >
          <i data-lucide="aperture" class="w-5 h-5"></i>
          {{ t('uploadModal.captureBtn') }}
        </button>
      </div>

      <!-- 3. Pre-made Sample Photos -->
      <div v-if="uploadTab === 'samples'" class="space-y-3">
        <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('uploadModal.samplesHelp') }}</p>
        <div class="grid grid-cols-3 gap-2 sm:gap-3">
          <button 
            v-for="sample in sampleList" 
            :key="sample.name"
            @click="selectSample(sample)"
            class="group relative rounded-xl overflow-hidden border border-slate-300 dark:border-slate-700 hover:border-emerald-500 text-left transition aspect-square bg-slate-100 dark:bg-slate-950 cursor-pointer"
          >
            <img :src="sample.url" :alt="sample.name" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
            <div class="absolute inset-0 bg-gradient-to-t from-slate-950/90 via-transparent to-transparent flex items-end p-2">
              <span class="text-[10px] sm:text-xs font-bold text-white truncate">{{ sample.name }}</span>
            </div>
          </button>
        </div>
      </div>
    </div>

    <!-- Modal Footer -->
    <template #footer>
      <div class="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400">
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
import { ref, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useInventoryStore } from '../../stores/inventory'
import type { SampleImage } from '../../types/inventory'
import BaseModal from '../common/BaseModal.vue'

const store = useInventoryStore()
const { t } = useI18n()

const uploadTab = ref<'file' | 'stream' | 'samples'>('file')
const isDragging = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const videoRef = ref<HTMLVideoElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const cameraLoading = ref(false)
let mediaStream: MediaStream | null = null

const sampleList: SampleImage[] = [
  {
    name: 'SMD Resistor',
    url: 'https://images.unsplash.com/photo-1592659762303-90081d34b277?w=400&q=80',
    type: 'Resistor',
    partNumber: 'RC0805',
  },
  {
    name: 'STM32 MCU',
    url: 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400&q=80',
    type: 'IC',
    partNumber: 'STM32F103',
  },
  {
    name: 'DIP-8 Timer',
    url: 'https://images.unsplash.com/photo-1608564697071-ddf911d81370?w=400&q=80',
    type: 'IC',
    partNumber: 'NE555',
  },
]

const switchTab = async (tab: 'file' | 'stream' | 'samples') => {
  uploadTab.value = tab
  if (tab === 'stream') {
    await initCamera()
  } else {
    stopCamera()
  }
}

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files[0]) {
    processFile(target.files[0])
  }
}

const handleDrop = (e: DragEvent) => {
  isDragging.value = false
  if (e.dataTransfer?.files && e.dataTransfer.files[0]) {
    processFile(e.dataTransfer.files[0])
  }
}

const processFile = (file: File) => {
  store.uploadFileToQueue(file)
  handleClose()
}

const selectSample = (sample: SampleImage) => {
  store.addQueueJob(sample.url)
  handleClose()
}

const initCamera = async () => {
  cameraLoading.value = true
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } },
      audio: false,
    })
    await nextTick()
    if (videoRef.value) {
      videoRef.value.srcObject = mediaStream
    }
  } catch (err) {
    console.error('Camera access error:', err)
    store.showToast(t('uploadModal.cameraError'))
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
    store.addQueueJob(photoUrl)
    handleClose()
  }
}

const stopCamera = () => {
  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop())
    mediaStream = null
  }
}

const handleClose = () => {
  stopCamera()
  store.showUploadModal = false
}

onUnmounted(() => {
  stopCamera()
})
</script>
