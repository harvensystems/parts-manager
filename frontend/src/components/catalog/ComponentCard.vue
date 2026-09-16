<template>
  <div class="glass-card rounded-lg overflow-hidden hover:border-slate-400 dark:hover:border-slate-700 transition duration-200 flex flex-col group relative text-xs shadow-xs">
    
    <!-- Component Image Preview & Badges -->
    <div class="relative h-28 bg-slate-100 dark:bg-slate-900 overflow-hidden border-b border-slate-200 dark:border-slate-800">
      <img 
        :src="item.photo || placeholderImg" 
        :alt="item.name"
        class="w-full h-full object-cover group-hover:scale-105 transition duration-300 opacity-90 group-hover:opacity-100"
      >
      <!-- Overlay Badges -->
      <div class="absolute top-1.5 left-1.5 flex flex-wrap gap-1">
        <span class="px-1.5 py-0.5 rounded text-[9px] font-semibold tracking-wide uppercase shadow bg-white/90 dark:bg-slate-900/80 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 backdrop-blur-sm">
          {{ t(`types.${item.type}`) }}
        </span>
        <span 
          v-if="item.mounting" 
          class="px-1.5 py-0.5 rounded text-[9px] font-mono font-semibold shadow backdrop-blur-sm"
          :class="item.mounting === 'SMD' ? 'bg-indigo-100/90 dark:bg-indigo-950/80 text-indigo-700 dark:text-indigo-300 border border-indigo-300 dark:border-indigo-700/40' : 'bg-amber-100/90 dark:bg-amber-950/80 text-amber-700 dark:text-amber-300 border border-amber-300 dark:border-amber-700/40'"
        >
          {{ item.mounting }}
        </span>
      </div>

      <!-- Quantity Badge -->
      <div class="absolute top-1.5 right-1.5">
        <span 
          class="px-1.5 py-0.5 rounded-full text-[10px] font-mono font-bold shadow-lg backdrop-blur-md flex items-center gap-1 border"
          :class="item.quantity > store.lowStockThreshold ? 'bg-white/95 dark:bg-slate-950/90 text-slate-800 dark:text-white border-slate-300 dark:border-slate-700' : 'bg-rose-100/95 dark:bg-rose-950/90 text-rose-700 dark:text-rose-300 border-rose-300 dark:border-rose-800 animate-pulse'"
        >
          {{ item.quantity }} {{ t('catalog.pcs') }}
        </span>
      </div>

      <!-- Package & Part Number Footer Tag on Image -->
      <div class="absolute bottom-1 left-1 right-1 flex justify-between items-center text-[10px] font-mono text-slate-700 dark:text-slate-300 bg-white/90 dark:bg-slate-950/85 backdrop-blur-sm px-1.5 py-0.5 rounded border border-slate-200 dark:border-slate-800">
        <span class="truncate font-semibold text-slate-800 dark:text-slate-200 max-w-[65%]">{{ item.partNumber || 'N/A' }}</span>
        <span class="text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800 px-1 py-0.2 rounded text-[9px]">{{ item.package || t('catalog.pkgUnknown') }}</span>
      </div>
    </div>

    <!-- Component Details Card Body -->
    <div class="p-2.5 flex-1 flex flex-col justify-between space-y-2">
      <div>
        <h3 class="font-bold text-slate-900 dark:text-white text-xs group-hover:text-emerald-600 dark:group-hover:text-emerald-400 transition line-clamp-1" :title="item.name">
          {{ item.name }}
        </h3>
        <p class="text-[11px] text-slate-500 dark:text-slate-400 line-clamp-1 mt-0.5">
          <span class="text-slate-700 dark:text-slate-300">{{ item.manufacturer || t('catalog.noBrand') }}</span>
        </p>
      </div>

      <!-- Metadata Key-Value Quick Chips -->
      <div v-if="item.metadata && Object.keys(item.metadata).length > 0" class="pt-1.5 border-t border-slate-200 dark:border-slate-800/80">
        <div class="flex flex-wrap gap-1">
          <span 
            v-for="(val, key) in limitedMetadata" 
            :key="key"
            class="px-1.5 py-0.5 bg-slate-100 dark:bg-slate-900 rounded border border-slate-200 dark:border-slate-800 text-[10px] font-mono text-slate-600 dark:text-slate-300 flex items-center gap-1"
          >
            <span class="text-slate-400 dark:text-slate-500">{{ key }}:</span>
            <span class="text-emerald-600 dark:text-emerald-300 font-medium truncate max-w-[55px]">{{ val }}</span>
          </span>
          <span 
            v-if="extraMetaCount > 0" 
            class="px-1 py-0.5 text-[9px] text-slate-400 dark:text-slate-500 bg-slate-100 dark:bg-slate-900 rounded"
          >
            +{{ extraMetaCount }}
          </span>
        </div>
      </div>

      <!-- Card Bottom Quick Stock Actions -->
      <div class="pt-2 border-t border-slate-200 dark:border-slate-800/80 flex items-center justify-between gap-1">
        <div class="flex items-center space-x-0.5 bg-slate-100 dark:bg-slate-900 rounded-md p-0.5 border border-slate-200 dark:border-slate-800">
          <button 
            @click.stop="store.adjustQuantity(item, -1)" 
            class="w-5 h-5 flex items-center justify-center rounded hover:bg-slate-200 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-white transition text-xs cursor-pointer"
            :title="t('catalog.decreaseQty')"
          >
            -
          </button>
          <span class="w-6 text-center font-mono font-semibold text-[11px] text-slate-800 dark:text-white">{{ item.quantity }}</span>
          <button 
            @click.stop="store.adjustQuantity(item, 1)" 
            class="w-5 h-5 flex items-center justify-center rounded hover:bg-slate-200 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-white transition text-xs cursor-pointer"
            :title="t('catalog.increaseQty')"
          >
            +
          </button>
        </div>

        <button 
          @click="store.openDetailModalForComponent(item)"
          class="px-2 py-1 bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 text-[11px] font-medium rounded-md transition flex items-center gap-1 border border-slate-300 dark:border-slate-700/50 cursor-pointer shadow-xs"
        >
          <i data-lucide="info" class="w-3 h-3 text-emerald-600 dark:text-emerald-400"></i>
          {{ t('catalog.details') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useInventoryStore } from '../../stores/inventory'
import type { ComponentItem } from '../../types/inventory'

const props = defineProps<{
  item: ComponentItem
}>()

const store = useInventoryStore()
const { t } = useI18n()

const placeholderImg = 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400&q=80'

const limitedMetadata = computed(() => {
  const entries = Object.entries(props.item.metadata || {})
  return Object.fromEntries(entries.slice(0, 2))
})

const extraMetaCount = computed(() => {
  const total = Object.keys(props.item.metadata || {}).length
  return Math.max(0, total - 2)
})
</script>
