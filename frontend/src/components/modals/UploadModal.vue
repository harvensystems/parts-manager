<template>
  <BaseModal
    v-model="store.showUploadModal"
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
          :class="activeTab === 'manual' ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'"
        >
          <i data-lucide="edit-3" class="w-3.5 h-3.5"></i>
          <span>{{ t('uploadModal.tabManual') }}</span>
        </button>
        <button 
          @click="switchTab('auto')"
          class="flex-1 py-2 text-center rounded-lg font-medium transition cursor-pointer flex items-center justify-center gap-1.5 relative"
          :class="activeTab === 'auto' ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 shadow-xs' : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200'"
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
      <div v-if="activeTab === 'manual'" class="space-y-4 text-xs sm:text-sm">
        <!-- Photo Attachment Row -->
        <div class="flex flex-col sm:flex-row items-center gap-3 p-3 bg-slate-50 dark:bg-slate-950/70 rounded-xl border border-slate-200 dark:border-slate-800">
          <div class="relative w-24 h-24 sm:w-20 sm:h-20 rounded-lg bg-slate-200 dark:bg-slate-800 border border-slate-300 dark:border-slate-700 flex items-center justify-center overflow-hidden shrink-0">
            <img 
              v-if="manualImagePreview" 
              :src="manualImagePreview" 
              alt="Part Preview" 
              class="w-full h-full object-cover"
            />
            <i v-else data-lucide="image" class="w-8 h-8 text-slate-400 dark:text-slate-500"></i>
          </div>

          <div class="flex-1 space-y-1.5 text-center sm:text-left w-full">
            <p class="font-semibold text-slate-800 dark:text-slate-200 text-xs sm:text-sm">
              {{ manualImagePreview ? t('uploadModal.changePhoto') : t('uploadModal.attachPhoto') }}
            </p>
            <p class="text-[11px] text-slate-500 dark:text-slate-400">
              {{ t('uploadModal.dropzoneSubtitle') }}
            </p>
            <div class="flex flex-wrap items-center justify-center sm:justify-start gap-2 pt-1">
              <button 
                type="button" 
                @click="triggerManualFileInput"
                class="px-3 py-1.5 bg-slate-200 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 rounded-lg text-xs font-medium transition cursor-pointer flex items-center gap-1.5"
              >
                <i data-lucide="upload" class="w-3.5 h-3.5"></i>
                <span>{{ manualImagePreview ? t('uploadModal.changePhoto') : t('uploadModal.attachPhoto') }}</span>
              </button>
              <button 
                v-if="manualImagePreview"
                type="button" 
                @click="removeManualImage"
                class="px-3 py-1.5 bg-rose-100 hover:bg-rose-200 dark:bg-rose-950/60 dark:hover:bg-rose-900/80 text-rose-700 dark:text-rose-300 rounded-lg text-xs font-medium transition cursor-pointer flex items-center gap-1.5"
              >
                <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
                <span>{{ t('uploadModal.removePhoto') }}</span>
              </button>
            </div>
            <input 
              ref="manualFileInputRef" 
              type="file" 
              accept="image/*" 
              class="hidden" 
              @change="handleManualFileSelect"
            />
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
          <!-- Name -->
          <div class="sm:col-span-2 space-y-1">
            <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.partName') }}</label>
            <input 
              v-model="manualForm.name" 
              type="text" 
              :placeholder="t('verifyModal.namePlaceholder')"
              class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
            >
          </div>

          <!-- Type -->
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.componentType') }}</label>
            <select 
              v-model="manualForm.type" 
              class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
            >
              <option v-for="tName in store.componentTypes" :key="tName" :value="tName">
                {{ t(`types.${tName}`) }}
              </option>
            </select>
          </div>

          <!-- Part Number -->
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.partNumber') }}</label>
            <input 
              v-model="manualForm.partNumber" 
              type="text" 
              :placeholder="t('verifyModal.partNumberPlaceholder')"
              class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white font-mono text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
            >
          </div>

          <!-- Manufacturer -->
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.manufacturer') }}</label>
            <input 
              v-model="manualForm.manufacturer" 
              type="text" 
              :placeholder="t('verifyModal.manufacturerPlaceholder')"
              class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
            >
          </div>

          <!-- Package -->
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.package') }}</label>
            <input 
              v-model="manualForm.package" 
              type="text" 
              :placeholder="t('verifyModal.packagePlaceholder')"
              class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white font-mono text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
            >
          </div>

          <!-- Mounting Type -->
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.mountingType') }}</label>
            <div class="flex gap-2">
              <button 
                type="button" 
                @click="manualForm.mounting = 'SMD'"
                class="flex-1 py-2 text-center rounded-lg border text-xs font-semibold transition cursor-pointer"
                :class="manualForm.mounting === 'SMD' ? 'bg-indigo-100 dark:bg-indigo-950/80 border-indigo-500 text-indigo-700 dark:text-indigo-300 shadow-xs' : 'bg-slate-50 dark:bg-slate-950 border-slate-300 dark:border-slate-800 text-slate-600 dark:text-slate-400'"
              >
                SMD
              </button>
              <button 
                type="button" 
                @click="manualForm.mounting = 'Through-hole'"
                class="flex-1 py-2 text-center rounded-lg border text-xs font-semibold transition cursor-pointer"
                :class="manualForm.mounting === 'Through-hole' ? 'bg-amber-100 dark:bg-amber-950/80 border-amber-500 text-amber-700 dark:text-amber-300 shadow-xs' : 'bg-slate-50 dark:bg-slate-950 border-slate-300 dark:border-slate-800 text-slate-600 dark:text-slate-400'"
              >
                Through-hole (THT)
              </button>
            </div>
          </div>

          <!-- Quantity Counter -->
          <div class="space-y-1">
            <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.quantity') }}</label>
            <div class="flex items-center space-x-2">
              <button 
                type="button" 
                @click="manualForm.quantity = Math.max(1, (manualForm.quantity || 1) - 1)"
                class="w-9 h-9 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 hover:border-slate-400 dark:hover:border-slate-500 text-slate-800 dark:text-white font-bold rounded-lg transition cursor-pointer flex items-center justify-center text-sm"
              >
                -
              </button>
              <input 
                v-model.number="manualForm.quantity" 
                type="number" 
                min="1"
                class="flex-1 py-2 text-center bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white font-mono font-bold text-sm focus:outline-none focus:border-emerald-500"
              >
              <button 
                type="button" 
                @click="manualForm.quantity = (manualForm.quantity || 1) + 1"
                class="w-9 h-9 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 hover:border-slate-400 dark:hover:border-slate-500 text-slate-800 dark:text-white font-bold rounded-lg transition cursor-pointer flex items-center justify-center text-sm"
              >
                +
              </button>
            </div>
          </div>
        </div>

        <!-- Description -->
        <div class="space-y-1">
          <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.description') }}</label>
          <textarea 
            v-model="manualForm.description" 
            rows="2"
            :placeholder="t('verifyModal.descriptionPlaceholder')"
            class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
          ></textarea>
        </div>

        <!-- Dynamic Metadata Key-Values -->
        <div class="space-y-2 pt-2 border-t border-slate-200 dark:border-slate-800">
          <div class="flex items-center justify-between">
            <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.metadataTitle') }}</label>
            <button 
              type="button" 
              @click="addMetadataRow" 
              class="text-xs text-emerald-600 dark:text-emerald-400 hover:underline flex items-center gap-1 cursor-pointer"
            >
              <i data-lucide="plus" class="w-3.5 h-3.5"></i> {{ t('verifyModal.addParam') }}
            </button>
          </div>

          <div class="space-y-2">
            <div 
              v-for="(row, idx) in metadataRows" 
              :key="idx" 
              class="flex items-center space-x-2"
            >
              <input 
                v-model="row.key" 
                type="text" 
                :placeholder="t('verifyModal.paramKeyPlaceholder')"
                class="w-1/2 px-3 py-1.5 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs font-mono focus:outline-none focus:border-emerald-500"
              >
              <input 
                v-model="row.value" 
                type="text" 
                :placeholder="t('verifyModal.paramValPlaceholder')"
                class="w-1/2 px-3 py-1.5 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs font-mono focus:outline-none focus:border-emerald-500"
              >
              <button 
                type="button" 
                @click="removeMetadataRow(idx)" 
                class="p-1.5 text-slate-400 hover:text-rose-600 dark:text-slate-500 dark:hover:text-rose-400 transition cursor-pointer"
              >
                <i data-lucide="trash-2" class="w-4 h-4"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. Combined AI Recognition Tab: Live Camera + File Upload / Drag & Drop -->
      <div v-if="activeTab === 'auto'" class="space-y-3">
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
      <div v-if="activeTab === 'manual'" class="flex items-center justify-between gap-2">
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
import { useInventoryStore } from '../../stores/inventory'
import { api } from '@/api'
import type { ComponentItem } from '../../types/inventory'
import BaseModal from '../common/BaseModal.vue'

const store = useInventoryStore()
const { t } = useI18n()

const activeTab = ref<'manual' | 'auto'>('manual')
const isDragging = ref(false)
const manualFileInputRef = ref<HTMLInputElement | null>(null)
const autoFileInputRef = ref<HTMLInputElement | null>(null)
const videoRef = ref<HTMLVideoElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const cameraLoading = ref(false)
const cameraActive = ref(false)
const isSavingManual = ref(false)
let mediaStream: MediaStream | null = null

// Manual form state
const manualSelectedFile = ref<File | null>(null)
const manualImagePreview = ref<string>('')

interface MetaRow {
  key: string
  value: string
}

const manualForm = ref<Partial<ComponentItem>>({
  name: '',
  type: 'Resistor',
  manufacturer: '',
  partNumber: '',
  package: '',
  mounting: 'SMD',
  quantity: 1,
  description: '',
})

const metadataRows = ref<MetaRow[]>([])

const addMetadataRow = () => {
  metadataRows.value.push({ key: '', value: '' })
}

const removeMetadataRow = (idx: number) => {
  metadataRows.value.splice(idx, 1)
}

const resetManualForm = () => {
  manualForm.value = {
    name: '',
    type: 'Resistor',
    manufacturer: '',
    partNumber: '',
    package: '',
    mounting: 'SMD',
    quantity: 1,
    description: '',
  }
  metadataRows.value = []
  manualSelectedFile.value = null
  manualImagePreview.value = ''
}

const getImageUrl = (id: string) => {
  return "/api/images/" + id
}

const triggerManualFileInput = () => {
  manualFileInputRef.value?.click()
}

const handleManualFileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    manualSelectedFile.value = file
    const reader = new FileReader()
    reader.onload = (event) => {
      manualImagePreview.value = (event.target?.result as string) || ''
    }
    reader.readAsDataURL(file)
  }
}

const removeManualImage = () => {
  manualSelectedFile.value = null
  manualImagePreview.value = ''
  if (manualFileInputRef.value) {
    manualFileInputRef.value.value = ''
  }
}

const saveManual = async () => {
  if (!manualForm.value.name?.trim()) {
    store.showToast(t('verifyModal.nameRequired'))
    return
  }

  isSavingManual.value = true
  try {
    let photoUrl = manualImagePreview.value

    // Upload attached image to backend GridFS if file selected
    if (manualSelectedFile.value) {
      try {
        const uploadRes = await api.images.uploadImage({ file: manualSelectedFile.value })
        if (uploadRes?.id) {
          photoUrl = getImageUrl(uploadRes.id)
        }
      } catch (uploadErr) {
        console.warn('Image upload failed, proceeding with data url fallback:', uploadErr)
      }
    }

    const metaObj: Record<string, any> = {}
    metadataRows.value.forEach((r) => {
      if (r.key.trim() && r.value.trim()) {
        metaObj[r.key.trim()] = r.value.trim()
      }
    })

    await store.saveComponent({
      ...manualForm.value,
      photo: photoUrl,
      metadata: metaObj,
    })

    resetManualForm()
    handleClose()
    store.currentView = 'catalog'
  } finally {
    isSavingManual.value = false
  }
}

// Auto Tab Actions
const switchTab = async (tab: 'manual' | 'auto') => {
  activeTab.value = tab
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
  store.uploadFileToQueue(file)
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
      cameraActive.value = true
    }
  } catch (err) {
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
    store.addQueueJob(photoUrl)
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
  store.showUploadModal = false
}

watch(
  () => store.showUploadModal,
  (show) => {
    if (!show) {
      stopCamera()
    } else if (activeTab.value === 'auto') {
      initCamera()
    }
  }
)

onUnmounted(() => {
  stopCamera()
})
</script>
