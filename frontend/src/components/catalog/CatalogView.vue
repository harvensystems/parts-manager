<template>
  <div class="space-y-4 sm:space-y-6">
    
    <!-- Search & Filters Toolbar -->
    <div class="glass-card rounded-xl p-3.5 sm:p-4 space-y-3 sm:space-y-4">
      <div class="flex flex-col md:flex-row gap-2.5 sm:gap-3">
        <!-- Main Search Input -->
        <div class="relative flex-1">
          <i data-lucide="search" class="w-4 h-4 absolute left-3.5 top-3.5 text-slate-400"></i>
          <input 
            v-model="store.searchQuery" 
            type="text" 
            :placeholder="t('catalog.searchPlaceholder')" 
            class="w-full pl-10 pr-10 py-2.5 bg-slate-900 border border-slate-700/80 rounded-lg text-xs sm:text-sm text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition font-mono"
          >
          <button 
            v-if="store.searchQuery" 
            @click="store.searchQuery = ''"
            class="absolute right-3 top-3 text-slate-400 hover:text-white cursor-pointer"
          >
            <i data-lucide="x" class="w-4 h-4"></i>
          </button>
        </div>

        <!-- Filters Grid (Mobile: 2 cols + 1 full, Desktop: 3 flex) -->
        <div class="grid grid-cols-2 sm:grid-cols-3 md:flex gap-2">
          <!-- Type Filter -->
          <select 
            v-model="store.selectedTypeFilter" 
            class="bg-slate-900 border border-slate-700/80 rounded-lg px-2.5 sm:px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
          >
            <option value="">{{ t('catalog.allTypes') }}</option>
            <option v-for="tName in store.componentTypes" :key="tName" :value="tName">
              {{ t(`types.${tName}`) }}
            </option>
          </select>

          <!-- Mounting Filter -->
          <select 
            v-model="store.selectedMountingFilter" 
            class="bg-slate-900 border border-slate-700/80 rounded-lg px-2.5 sm:px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
          >
            <option value="">{{ t('catalog.anyMounting') }}</option>
            <option value="SMD">SMD</option>
            <option value="Through-hole">Through-hole (THT)</option>
          </select>

          <!-- Sort Filter -->
          <select 
            v-model="store.sortBy" 
            class="col-span-2 sm:col-span-1 bg-slate-900 border border-slate-700/80 rounded-lg px-2.5 sm:px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
          >
            <option value="updatedAt">{{ t('catalog.sortNewest') }}</option>
            <option value="name">{{ t('catalog.sortName') }}</option>
            <option value="quantity">{{ t('catalog.sortQuantity') }}</option>
          </select>
        </div>
      </div>

      <!-- Quick Filter Tags -->
      <div class="flex flex-wrap items-center gap-1.5 pt-2 border-t border-slate-800/60 text-xs">
        <span class="text-slate-500 mr-1 flex items-center gap-1 text-[11px] sm:text-xs">
          <i data-lucide="filter" class="w-3 h-3"></i> {{ t('catalog.quickFilters') }}
        </span>
        <button 
          v-for="tag in ['Resistor', 'Capacitor', 'IC', 'Transistor', 'Diode', 'SMD', 'Through-hole', '5V', '10k']" 
          :key="tag"
          @click="store.setQuickTag(tag)"
          class="px-2 py-0.5 rounded border text-[11px] sm:text-xs text-slate-300 hover:border-emerald-500/50 hover:text-emerald-400 transition cursor-pointer"
          :class="store.searchQuery.includes(tag) || store.selectedTypeFilter === tag || store.selectedMountingFilter === tag ? 'bg-emerald-500/10 border-emerald-500 text-emerald-400 font-medium' : 'bg-slate-900/60 border-slate-800'"
        >
          {{ store.componentTypes.includes(tag) ? t(`types.${tag}`) : tag }}
        </button>
        <button 
          v-if="store.searchQuery || store.selectedTypeFilter || store.selectedMountingFilter"
          @click="store.clearAllFilters"
          class="text-amber-400 hover:underline ml-auto text-[11px] cursor-pointer"
        >
          {{ t('catalog.clearFilters') }}
        </button>
      </div>
    </div>

    <!-- Catalog Component Grid -->
    <div v-if="store.filteredComponents.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-2.5 sm:gap-3">
      <ComponentCard 
        v-for="item in store.filteredComponents" 
        :key="item.id" 
        :item="item" 
      />
    </div>

    <!-- Empty State -->
    <div v-else class="glass-card rounded-2xl p-8 sm:p-12 text-center max-w-md mx-auto my-6 sm:my-8">
      <div class="w-14 h-14 sm:w-16 sm:h-16 bg-slate-900 rounded-full flex items-center justify-center mx-auto text-slate-500 border border-slate-800 mb-3 sm:mb-4">
        <i data-lucide="search-x" class="w-7 h-7 sm:w-8 sm:h-8"></i>
      </div>
      <h3 class="text-base sm:text-lg font-semibold text-white">{{ t('catalog.emptyTitle') }}</h3>
      <p class="text-xs sm:text-sm text-slate-400 mt-1">
        {{ t('catalog.emptyText') }}
      </p>
      <button 
        @click="store.showUploadModal = true" 
        class="mt-4 sm:mt-5 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-slate-950 font-semibold text-xs sm:text-sm rounded-lg shadow inline-flex items-center gap-2 transition cursor-pointer"
      >
        <i data-lucide="camera" class="w-4 h-4"></i>
        {{ t('catalog.takePhoto') }}
      </button>
    </div>

  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useInventoryStore } from '../../stores/inventory'
import ComponentCard from './ComponentCard.vue'

const store = useInventoryStore()
const { t } = useI18n()
</script>
