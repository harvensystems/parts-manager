<template>
  <BaseModal
    v-model="uiStore.showDetailModal"
    :title="t('detailModal.title')"
    :subtitle="catalogStore.selectedComponent?.partNumber || catalogStore.selectedComponent?.name"
    icon="info"
    maxWidth="xl"
    @close="closeModal"
  >
    <div v-if="catalogStore.selectedComponent" class="space-y-4 text-xs sm:text-sm">
      <!-- Badges & Quantity Bar -->
      <div class="flex flex-wrap items-center justify-between gap-2 border-b border-slate-200 dark:border-slate-800 pb-3">
        <div class="flex items-center gap-2">
          <span class="px-2 py-0.5 rounded text-xs font-bold uppercase tracking-wider bg-emerald-100 dark:bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border border-emerald-300 dark:border-emerald-500/30">
            {{ t(`types.${catalogStore.selectedComponent.type}`) }}
          </span>
          <span 
            v-if="catalogStore.selectedComponent.mounting" 
            class="px-2 py-0.5 rounded text-xs font-mono font-semibold"
            :class="catalogStore.selectedComponent.mounting === 'SMD' ? 'bg-indigo-100 dark:bg-indigo-950 text-indigo-700 dark:text-indigo-300 border border-indigo-300 dark:border-indigo-700/40' : 'bg-amber-100 dark:bg-amber-950 text-amber-700 dark:text-amber-300 border border-amber-300 dark:border-amber-700/40'"
          >
            {{ catalogStore.selectedComponent.mounting }}
          </span>
          <span v-if="catalogStore.selectedComponent.packageType" class="px-2 py-0.5 rounded text-xs font-mono bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border border-slate-200 dark:border-transparent">
            {{ catalogStore.selectedComponent.packageType }}
          </span>
        </div>

        <div class="flex items-center space-x-2 bg-slate-100 dark:bg-slate-950 p-1 rounded-lg border border-slate-200 dark:border-slate-800">
          <button 
            @click="catalogStore.adjustQuantity(catalogStore.selectedComponent, -1)" 
            class="w-7 h-7 flex items-center justify-center rounded hover:bg-slate-200 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-white text-sm cursor-pointer"
            :title="t('catalog.decreaseQty')"
          >
            -
          </button>
          <span class="px-2 font-mono font-bold text-emerald-600 dark:text-emerald-400 text-sm">{{ catalogStore.selectedComponent.quantity }} {{ t('detailModal.pcs') }}</span>
          <button 
            @click="catalogStore.adjustQuantity(catalogStore.selectedComponent, 1)" 
            class="w-7 h-7 flex items-center justify-center rounded hover:bg-slate-200 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-white text-sm cursor-pointer"
            :title="t('catalog.increaseQty')"
          >
            +
          </button>
        </div>
      </div>

      <!-- Component Name & Manufacturer -->
      <div>
        <h2 class="text-base sm:text-lg font-bold text-slate-900 dark:text-white">{{ catalogStore.selectedComponent.name }}</h2>
        <p class="text-xs text-slate-500 dark:text-slate-400 font-mono mt-0.5">
          {{ t('detailModal.manufacturer') }} <strong class="text-slate-700 dark:text-slate-200">{{ catalogStore.selectedComponent.manufacturer || t('detailModal.notSpecified') }}</strong>
        </p>
      </div>

      <!-- Image Display -->
      <div v-if="catalogStore.selectedComponent.photoIds" class="rounded-xl overflow-hidden bg-slate-100 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 h-48 sm:h-56">
        <img :src="catalogStore.getImageUrl(catalogStore.selectedComponent.photoIds)" :alt="catalogStore.selectedComponent.name" class="w-full h-full object-cover">
      </div>

      <!-- Description -->
      <div v-if="catalogStore.selectedComponent.description" class="space-y-1">
        <h4 class="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">{{ t('detailModal.description') }}</h4>
        <p class="text-slate-700 dark:text-slate-200 bg-slate-50 dark:bg-slate-950 p-3 rounded-lg border border-slate-200 dark:border-slate-800/80 leading-relaxed">
          {{ catalogStore.selectedComponent.description }}
        </p>
      </div>

      <!-- Metadata Properties Grid -->
      <div v-if="catalogStore.selectedComponent.metadata && Object.keys(catalogStore.selectedComponent.metadata).length > 0" class="space-y-2">
        <h4 class="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">{{ t('detailModal.metadataTitle') }}</h4>
        <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
          <div 
            v-for="(val, key) in catalogStore.selectedComponent.metadata" 
            :key="key"
            class="bg-slate-50 dark:bg-slate-950 p-2.5 rounded-lg border border-slate-200 dark:border-slate-800 font-mono text-xs"
          >
            <div class="text-slate-400 dark:text-slate-500 text-[10px] truncate">{{ key }}</div>
            <div class="text-emerald-600 dark:text-emerald-300 font-semibold mt-0.5 truncate">{{ val }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Footer -->
    <template #footer>
      <div class="flex items-center justify-between">
        <button 
          @click="confirmDelete" 
          class="px-3 py-2 text-rose-600 dark:text-rose-400 hover:text-rose-700 dark:hover:text-rose-300 hover:bg-rose-50 dark:hover:bg-rose-950/30 rounded-lg text-xs font-semibold transition flex items-center gap-1.5 cursor-pointer"
        >
          <i data-lucide="trash-2" class="w-4 h-4"></i>
          {{ t('detailModal.deletePart') }}
        </button>

        <button 
          @click="closeModal" 
          class="px-4 py-2 bg-slate-200 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 font-medium text-xs sm:text-sm rounded-lg transition cursor-pointer"
        >
          {{ t('detailModal.close') }}
        </button>
      </div>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useUiStore } from '@/stores/ui'
import { useCatalogStore } from '@/stores/catalog'
import BaseModal from '@/components/common/BaseModal.vue'

const uiStore = useUiStore()
const catalogStore = useCatalogStore()
const { t } = useI18n()

const closeModal = () => {
  uiStore.showDetailModal = false
  catalogStore.selectedComponent = { }
}

const confirmDelete = () => {
  if (catalogStore.selectedComponent && confirm(t('detailModal.confirmDelete', { name: catalogStore.selectedComponent.name }))) {
    catalogStore.deleteComponent(catalogStore.selectedComponent.id!)
  }
}
</script>
