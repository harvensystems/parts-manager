<script setup lang="ts">

import SelectField from '@/components/SelectField.vue'
import { useCatalogStore } from '@/stores/catalog'
import { useI18n } from 'vue-i18n'
import { ref, watch } from 'vue'
import { CreateOrUpdatePartDto } from '@/api/api.ts'

const model = defineModel<{ dto: CreateOrUpdatePartDto, file: File | null }>()
const catalogStore = useCatalogStore()
const { t } = useI18n()
const fileInputRef = ref<HTMLInputElement | null>(null)

const params = ref<{ key: string, value: string }[]>(model.value?.dto.metadata ? Object.entries(model.value.dto.metadata).map(([key, value]) => ({ key, value })) : [])
const preview = ref<string>('')

const addParams = () => params.value.push({ key: '', value: '' })
const removeParams = (idx: number) => params.value.splice(idx, 1)

watch([params], () => {
  params.value.forEach((param) => {
    if (!param.key || !param.value) {
      return
    } else {
      if (!model.value?.dto?.metadata) model.value!.dto.metadata = {}
      model.value!.dto.metadata[param.key] = param.value
    }
  })
}, { deep: true })

const triggerManualFileInput = () => {
  fileInputRef.value?.click()
}

const fileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    model.value!.file = file
    const reader = new FileReader()
    reader.onload = (event) => {
      preview.value = (event.target?.result as string) || ''
    }
    reader.readAsDataURL(file)
  }
}

const removeImage = () => {
  model.value!.file = null
  preview.value = ''
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

</script>

<template>
  <div class="space-y-4 text-xs sm:text-sm">
    <!-- Photo Attachment Row -->
    <img v-if="model?.dto.photoIds?.length"
      :src="catalogStore.getImageUrl(model?.dto.photoIds)"
      alt="Preview"
      class="w-full sm:w-28 h-28 object-cover rounded-lg border border-slate-300 dark:border-slate-700 shadow shrink-0"
    >
    <div v-else
      class="flex flex-col sm:flex-row items-center gap-3 p-3 bg-slate-50 dark:bg-slate-950/70 rounded-xl border border-slate-200 dark:border-slate-800">
      <div
        class="relative w-24 h-24 sm:w-20 sm:h-20 rounded-lg bg-slate-200 dark:bg-slate-800 border border-slate-300 dark:border-slate-700 flex items-center justify-center overflow-hidden shrink-0">
        <img
          v-if="preview"
          :src="preview"
          alt="Part Preview"
          class="w-full h-full object-cover"
        />
<!--        <i v-if="!preview" data-lucide="image" class="w-8 h-8 text-slate-400 dark:text-slate-500"></i>-->
      </div>

      <div class="flex-1 space-y-1.5 text-center sm:text-left w-full">
        <p class="font-semibold text-slate-800 dark:text-slate-200 text-xs sm:text-sm">
          {{ preview ? t('uploadModal.changePhoto') : t('uploadModal.attachPhoto') }}
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
            <span>{{ preview ? t('uploadModal.changePhoto') : t('uploadModal.attachPhoto') }}</span>
          </button>
          <button
            v-if="preview"
            type="button"
            @click="removeImage"
            class="px-3 py-1.5 bg-rose-100 hover:bg-rose-200 dark:bg-rose-950/60 dark:hover:bg-rose-900/80 text-rose-700 dark:text-rose-300 rounded-lg text-xs font-medium transition cursor-pointer flex items-center gap-1.5"
          >
            <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
            <span>{{ t('uploadModal.removePhoto') }}</span>
          </button>
        </div>
        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          class="hidden"
          @change="fileSelect"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
      <!-- Name -->
      <div class="sm:col-span-2 space-y-1">
        <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">
          {{ t('verifyModal.partName') }}
        </label>
        <input
          v-model="model!.dto.name"
          type="text"
          :placeholder="t('verifyModal.namePlaceholder')"
          class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
        >
      </div>

      <!-- Type -->
      <SelectField v-model="model!.dto.type" :label="t('verifyModal.componentType')"
                   :items="catalogStore.dictionary.components!!" />

      <!-- Part Number -->
      <div class="space-y-1">
        <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.partNumber') }}</label>
        <input
          v-model="model!.dto.partNumber"
          type="text"
          :placeholder="t('verifyModal.partNumberPlaceholder')"
          class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white font-mono text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
        >
      </div>

      <!-- Manufacturer -->
      <SelectField v-model="model!.dto.manufacturer" :label="t('verifyModal.manufacturer')"
                   :items="catalogStore.dictionary.manufacturers!!"
                   :placeholder="t('verifyModal.manufacturerPlaceholder')" />

      <!-- Package -->
      <SelectField v-model="model!.dto.packageType" :label="t('verifyModal.package')"
                   :items="catalogStore.dictionary.packages!!" :placeholder="t('verifyModal.packagePlaceholder')" />

      <!-- Mounting Type -->
      <div class="space-y-1">
        <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.mountingType')
          }}</label>
        <div class="flex gap-2">
          <button
            type="button"
            @click="model!.dto.mounting = 'SMD'"
            class="flex-1 py-2 text-center rounded-lg border text-xs font-semibold transition cursor-pointer"
            :class="model?.dto.mounting === 'SMD' ? 'bg-indigo-100 dark:bg-indigo-950/80 border-indigo-500 text-indigo-700 dark:text-indigo-300 shadow-xs' : 'bg-slate-50 dark:bg-slate-950 border-slate-300 dark:border-slate-800 text-slate-600 dark:text-slate-400'"
          >
            SMD
          </button>
          <button
            type="button"
            @click="model!.dto.mounting = 'Through-hole'"
            class="flex-1 py-2 text-center rounded-lg border text-xs font-semibold transition cursor-pointer"
            :class="model?.dto.mounting === 'Through-hole' ? 'bg-amber-100 dark:bg-amber-950/80 border-amber-500 text-amber-700 dark:text-amber-300 shadow-xs' : 'bg-slate-50 dark:bg-slate-950 border-slate-300 dark:border-slate-800 text-slate-600 dark:text-slate-400'"
          >
            Through-hole (THT)
          </button>
        </div>
      </div>

      <!-- Quantity Counter -->
      <div class="space-y-1">
        <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.quantity')
          }}</label>
        <div class="flex items-center space-x-2">
          <button
            type="button"
            @click="model!.dto.quantity = Math.max(1, (model?.dto.quantity || 1) - 1)"
            class="w-9 h-9 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 hover:border-slate-400 dark:hover:border-slate-500 text-slate-800 dark:text-white font-bold rounded-lg transition cursor-pointer flex items-center justify-center text-sm"
          >
            -
          </button>
          <input
            v-model.number="model!.dto.quantity"
            type="number"
            min="1"
            class="flex-1 py-2 text-center bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white font-mono font-bold text-sm focus:outline-none focus:border-emerald-500"
          >
          <button
            type="button"
            @click="model!.dto.quantity !!= (model?.dto.quantity || 1) + 1"
            class="w-9 h-9 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 hover:border-slate-400 dark:hover:border-slate-500 text-slate-800 dark:text-white font-bold rounded-lg transition cursor-pointer flex items-center justify-center text-sm"
          >
            +
          </button>
        </div>
      </div>
    </div>

    <!-- Description -->
    <div class="space-y-1">
      <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.description')
        }}</label>
      <textarea
        v-model="model!.dto.description"
        rows="2"
        :placeholder="t('verifyModal.descriptionPlaceholder')"
        class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
      ></textarea>
    </div>

    <!-- Dynamic Metadata Key-Values -->
    <div class="space-y-2 pt-2 border-t border-slate-200 dark:border-slate-800">
      <div class="flex items-center justify-between">
        <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ t('verifyModal.metadataTitle')
          }}</label>
        <button
          type="button"
          @click="addParams"
          class="text-xs text-emerald-600 dark:text-emerald-400 hover:underline flex items-center gap-1 cursor-pointer"
        >
          <i data-lucide="plus" class="w-3.5 h-3.5"></i> {{ t('verifyModal.addParam') }}
        </button>
      </div>

      <div class="space-y-2">
        <div
          v-for="(row, idx) in params"
          :key="idx"
          class="flex items-center space-x-2"
        >
          <SelectField
            size="small"
            v-model="row.key"
            :placeholder="t('verifyModal.paramKeyPlaceholder')"
            :items="catalogStore.dictionary.parameters!!"
          />
          <input
            v-model="row.value"
            type="text"
            :placeholder="t('verifyModal.paramValPlaceholder')"
            class="w-1/2 px-3 py-1.5 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs font-mono focus:outline-none focus:border-emerald-500"
          >
          <button
            type="button"
            @click="removeParams(idx)"
            class="p-1.5 text-slate-400 hover:text-rose-600 dark:text-slate-500 dark:hover:text-rose-400 transition cursor-pointer"
          >
            <i data-lucide="trash-2" class="w-4 h-4"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>