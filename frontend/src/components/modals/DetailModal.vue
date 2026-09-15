<template>
  <BaseModal
    v-model="store.showDetailModal"
    :title="t('detailModal.title')"
    :subtitle="store.selectedComponent?.partNumber || store.selectedComponent?.name"
    icon="info"
    maxWidth="xl"
    @close="closeModal"
  >
    <div v-if="store.selectedComponent" class="space-y-4 text-xs sm:text-sm">
      <!-- Badges & Quantity Bar -->
      <div class="flex flex-wrap items-center justify-between gap-2 border-b border-slate-800 pb-3">
        <div class="flex items-center gap-2">
          <span class="px-2 py-0.5 rounded text-xs font-bold uppercase tracking-wider bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            {{ t(`types.${store.selectedComponent.type}`) }}
          </span>
          <span 
            v-if="store.selectedComponent.mounting" 
            class="px-2 py-0.5 rounded text-xs font-mono font-semibold"
            :class="store.selectedComponent.mounting === 'SMD' ? 'bg-indigo-950 text-indigo-300 border border-indigo-700/40' : 'bg-amber-950 text-amber-300 border border-amber-700/40'"
          >
            {{ store.selectedComponent.mounting }}
          </span>
          <span v-if="store.selectedComponent.package" class="px-2 py-0.5 rounded text-xs font-mono bg-slate-800 text-slate-300">
            {{ store.selectedComponent.package }}
          </span>
        </div>

        <div class="flex items-center space-x-2 bg-slate-950 p-1 rounded-lg border border-slate-800">
          <button 
            @click="store.adjustQuantity(store.selectedComponent, -1)" 
            class="w-7 h-7 flex items-center justify-center rounded hover:bg-slate-800 text-slate-400 hover:text-white text-sm cursor-pointer"
            :title="t('catalog.decreaseQty')"
          >
            -
          </button>
          <span class="px-2 font-mono font-bold text-emerald-400 text-sm">{{ store.selectedComponent.quantity }} {{ t('detailModal.pcs') }}</span>
          <button 
            @click="store.adjustQuantity(store.selectedComponent, 1)" 
            class="w-7 h-7 flex items-center justify-center rounded hover:bg-slate-800 text-slate-400 hover:text-white text-sm cursor-pointer"
            :title="t('catalog.increaseQty')"
          >
            +
          </button>
        </div>
      </div>

      <!-- Component Name & Manufacturer -->
      <div>
        <h2 class="text-base sm:text-lg font-bold text-white">{{ store.selectedComponent.name }}</h2>
        <p class="text-xs text-slate-400 font-mono mt-0.5">
          {{ t('detailModal.manufacturer') }} <strong class="text-slate-200">{{ store.selectedComponent.manufacturer || t('detailModal.notSpecified') }}</strong>
        </p>
      </div>

      <!-- Image Display -->
      <div v-if="store.selectedComponent.photo" class="rounded-xl overflow-hidden bg-slate-950 border border-slate-800 h-48 sm:h-56">
        <img :src="store.selectedComponent.photo" :alt="store.selectedComponent.name" class="w-full h-full object-cover">
      </div>

      <!-- Description -->
      <div v-if="store.selectedComponent.description" class="space-y-1">
        <h4 class="text-xs font-semibold text-slate-400 uppercase tracking-wider">{{ t('detailModal.description') }}</h4>
        <p class="text-slate-200 bg-slate-950 p-3 rounded-lg border border-slate-800/80 leading-relaxed">
          {{ store.selectedComponent.description }}
        </p>
      </div>

      <!-- Metadata Properties Grid -->
      <div v-if="store.selectedComponent.metadata && Object.keys(store.selectedComponent.metadata).length > 0" class="space-y-2">
        <h4 class="text-xs font-semibold text-slate-400 uppercase tracking-wider">{{ t('detailModal.metadataTitle') }}</h4>
        <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
          <div 
            v-for="(val, key) in store.selectedComponent.metadata" 
            :key="key"
            class="bg-slate-950 p-2.5 rounded-lg border border-slate-800 font-mono text-xs"
          >
            <div class="text-slate-500 text-[10px] truncate">{{ key }}</div>
            <div class="text-emerald-300 font-semibold mt-0.5 truncate">{{ val }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Footer -->
    <template #footer>
      <div class="flex items-center justify-between">
        <button 
          @click="confirmDelete" 
          class="px-3 py-2 text-rose-400 hover:text-rose-300 hover:bg-rose-950/30 rounded-lg text-xs font-semibold transition flex items-center gap-1.5 cursor-pointer"
        >
          <i data-lucide="trash-2" class="w-4 h-4"></i>
          {{ t('detailModal.deletePart') }}
        </button>

        <button 
          @click="closeModal" 
          class="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium text-xs sm:text-sm rounded-lg transition cursor-pointer"
        >
          {{ t('detailModal.close') }}
        </button>
      </div>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useInventoryStore } from '../../stores/inventory'
import BaseModal from '../common/BaseModal.vue'

const store = useInventoryStore()
const { t } = useI18n()

const closeModal = () => {
  store.showDetailModal = false
  store.selectedComponent = null
}

const confirmDelete = () => {
  if (store.selectedComponent && confirm(t('detailModal.confirmDelete', { name: store.selectedComponent.name }))) {
    store.deleteComponent(store.selectedComponent.id)
  }
}
</script>
