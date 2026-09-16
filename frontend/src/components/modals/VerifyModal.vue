<template>
  <BaseModal
    v-model="store.showVerifyModal"
    :title="t('verifyModal.title')"
    :subtitle="t('verifyModal.subtitle')"
    icon="check-circle"
    maxWidth="2xl"
    @close="closeModal"
  >
    <div v-if="store.activeVerifyJob" class="space-y-4 text-xs sm:text-sm">
      <!-- Photo Preview & Quick Confidence Banner -->
      <div class="flex flex-col sm:flex-row gap-3 sm:gap-4 items-start bg-slate-50 dark:bg-slate-950/70 p-3 rounded-xl border border-slate-200 dark:border-slate-800">
        <img 
          :src="store.activeVerifyJob.photoUrl" 
          alt="Preview" 
          class="w-full sm:w-28 h-28 object-cover rounded-lg border border-slate-300 dark:border-slate-700 shadow shrink-0"
        >
        <div class="flex-1 space-y-1.5 w-full min-w-0">
          <div class="flex items-center justify-between gap-2">
            <span class="text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.originalPhoto') }}</span>
            <span 
              v-if="store.activeVerifyJob.aiResult?.confidence" 
              class="px-2 py-0.5 bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-400 border border-emerald-300 dark:border-emerald-800/60 rounded text-[11px] font-mono font-bold shrink-0"
            >
              {{ store.activeVerifyJob.aiResult.confidence }}% {{ t('verifyModal.aiConfidence') }}
            </span>
          </div>
          <p v-if="store.activeVerifyJob.aiResult?.rawText" class="text-xs font-mono text-slate-700 dark:text-slate-400 bg-white dark:bg-slate-900 p-2 rounded border border-slate-200 dark:border-slate-800 break-words">
            <strong class="text-slate-500">{{ t('verifyModal.recognizedText') }}</strong> {{ store.activeVerifyJob.aiResult.rawText }}
          </p>
        </div>
      </div>

      <!-- Form Inputs Grid -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
        <!-- Name -->
        <div class="sm:col-span-2 space-y-1">
          <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.partName') }}</label>
          <input 
            v-model="form.name" 
            type="text" 
            :placeholder="t('verifyModal.namePlaceholder')"
            class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
          >
        </div>

        <!-- Type -->
        <div class="space-y-1">
          <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.componentType') }}</label>
          <select 
            v-model="form.type" 
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
            v-model="form.partNumber" 
            type="text" 
            :placeholder="t('verifyModal.partNumberPlaceholder')"
            class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white font-mono text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
          >
        </div>

        <!-- Manufacturer -->
        <div class="space-y-1">
          <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.manufacturer') }}</label>
          <input 
            v-model="form.manufacturer" 
            type="text" 
            :placeholder="t('verifyModal.manufacturerPlaceholder')"
            class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
          >
        </div>

        <!-- Package -->
        <div class="space-y-1">
          <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.package') }}</label>
          <input 
            v-model="form.package" 
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
              @click="form.mounting = 'SMD'"
              class="flex-1 py-2 text-center rounded-lg border text-xs font-semibold transition cursor-pointer"
              :class="form.mounting === 'SMD' ? 'bg-indigo-100 dark:bg-indigo-950/80 border-indigo-500 text-indigo-700 dark:text-indigo-300 shadow-xs' : 'bg-slate-50 dark:bg-slate-950 border-slate-300 dark:border-slate-800 text-slate-600 dark:text-slate-400'"
            >
              SMD
            </button>
            <button 
              type="button" 
              @click="form.mounting = 'Through-hole'"
              class="flex-1 py-2 text-center rounded-lg border text-xs font-semibold transition cursor-pointer"
              :class="form.mounting === 'Through-hole' ? 'bg-amber-100 dark:bg-amber-950/80 border-amber-500 text-amber-700 dark:text-amber-300 shadow-xs' : 'bg-slate-50 dark:bg-slate-950 border-slate-300 dark:border-slate-800 text-slate-600 dark:text-slate-400'"
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
              @click="form.quantity = Math.max(1, (form.quantity || 1) - 1)"
              class="w-9 h-9 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 hover:border-slate-400 dark:hover:border-slate-500 text-slate-800 dark:text-white font-bold rounded-lg transition cursor-pointer flex items-center justify-center text-sm"
            >
              -
            </button>
            <input 
              v-model.number="form.quantity" 
              type="number" 
              min="1"
              class="flex-1 py-2 text-center bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white font-mono font-bold text-sm focus:outline-none focus:border-emerald-500"
            >
            <button 
              type="button" 
              @click="form.quantity = (form.quantity || 1) + 1"
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
          v-model="form.description" 
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

    <!-- Modal Footer -->
    <template #footer>
      <div class="flex items-center justify-between gap-2">
        <button 
          type="button" 
          @click="closeModal" 
          class="px-4 py-2 bg-slate-200 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 font-medium text-xs sm:text-sm rounded-lg transition cursor-pointer"
        >
          {{ t('verifyModal.cancel') }}
        </button>

        <button 
          type="button" 
          @click="saveAndConfirm" 
          class="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white dark:text-slate-950 font-bold text-xs sm:text-sm rounded-lg shadow-lg shadow-emerald-950/60 transition flex items-center gap-1.5 cursor-pointer active:scale-95"
        >
          <i data-lucide="check" class="w-4 h-4"></i>
          {{ t('verifyModal.saveToCatalog') }}
        </button>
      </div>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useInventoryStore } from '../../stores/inventory'
import type { ComponentItem } from '../../types/inventory'
import BaseModal from '../common/BaseModal.vue'

const store = useInventoryStore()
const { t } = useI18n()

const form = ref<Partial<ComponentItem>>({
  name: '',
  type: 'Resistor',
  manufacturer: '',
  partNumber: '',
  package: '',
  mounting: 'SMD',
  quantity: 1,
  description: '',
})

interface MetaRow {
  key: string
  value: string
}

const metadataRows = ref<MetaRow[]>([])

watch(
  () => store.activeVerifyJob,
  (job) => {
    if (job && job.aiResult) {
      const res = job.aiResult
      form.value = {
        name: res.name || '',
        type: res.type || 'Resistor',
        manufacturer: res.manufacturer || '',
        partNumber: res.partNumber || '',
        package: res.package || '',
        mounting: (res.mounting as any) || 'SMD',
        quantity: res.quantity || 1,
        description: res.description || '',
      }

      metadataRows.value = Object.entries(res.metadata || {}).map(([key, value]) => ({
        key,
        value: String(value),
      }))
    } else {
      form.value = {
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
    }
  },
  { immediate: true }
)

const addMetadataRow = () => {
  metadataRows.value.push({ key: '', value: '' })
}

const removeMetadataRow = (idx: number) => {
  metadataRows.value.splice(idx, 1)
}

const closeModal = () => {
  store.showVerifyModal = false
  store.activeVerifyJob = null
}

const saveAndConfirm = () => {
  if (!form.value.name?.trim()) {
    store.showToast(t('verifyModal.nameRequired'))
    return
  }

  const metaObj: Record<string, any> = {}
  metadataRows.value.forEach((r) => {
    if (r.key.trim() && r.value.trim()) {
      metaObj[r.key.trim()] = r.value.trim()
    }
  })

  store.saveComponent({
    ...form.value,
    photo: store.activeVerifyJob?.photoUrl,
    metadata: metaObj,
  })

  if (store.activeVerifyJob) {
    store.removeQueueJob(store.activeVerifyJob.id)
  }

  closeModal()
  store.currentView = 'catalog'
}
</script>
