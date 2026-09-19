<template>
  <transition name="toast-fade">
    <div 
      v-if="uiStore.toastMessage" 
      class="fixed bottom-18 md:bottom-6 right-4 z-50 bg-white/95 dark:bg-slate-900/95 border border-emerald-500/40 text-emerald-700 dark:text-emerald-300 text-xs sm:text-sm px-4 py-2.5 rounded-xl shadow-2xl flex items-center gap-2 backdrop-blur-md"
    >
      <i data-lucide="check-circle" class="w-4 h-4 text-emerald-600 dark:text-emerald-400"></i>
      <span>{{ uiStore.toastMessage }}</span>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { useUiStore } from '../../stores/ui'

const uiStore = useUiStore()
</script>

<style scoped>
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.25s ease;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
