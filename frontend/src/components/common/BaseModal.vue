<template>
  <Teleport to="body">
    <transition name="modal-fade">
      <div 
        v-if="modelValue" 
        class="fixed inset-0 z-50 flex items-center justify-center sm:p-4 bg-slate-900/60 dark:bg-slate-950/85 backdrop-blur-md overflow-hidden"
        @click.self="onBackdropClick"
      >
        <!-- Modal Card: Full Screen on Mobile (<sm), Centered Card on sm+ -->
        <div 
          class="w-full h-full sm:h-auto sm:max-h-[90vh] bg-white dark:bg-slate-900 sm:border sm:border-slate-200 dark:sm:border-slate-700/80 sm:rounded-2xl shadow-2xl flex flex-col overflow-hidden transition-all duration-200"
          :class="maxWidthClass"
        >
          <!-- Modal Header -->
          <div class="px-4 py-3 sm:px-5 sm:py-4 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between bg-white/95 dark:bg-slate-900/95 sticky top-0 z-10 shrink-0">
            <slot name="header">
              <div class="flex items-center space-x-2.5">
                <div v-if="icon" class="p-2 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 rounded-lg">
                  <i :data-lucide="icon" class="w-5 h-5"></i>
                </div>
                <div>
                  <h3 class="font-bold text-slate-900 dark:text-white text-sm sm:text-base leading-snug">{{ title }}</h3>
                  <p v-if="subtitle" class="text-xs text-slate-500 dark:text-slate-400 leading-none mt-0.5">{{ subtitle }}</p>
                </div>
              </div>
            </slot>

            <button 
              @click="close" 
              class="p-2 -mr-1 text-slate-400 hover:text-slate-700 dark:hover:text-white rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition cursor-pointer"
              title="Закрити"
            >
              <i data-lucide="x" class="w-5 h-5"></i>
            </button>
          </div>

          <!-- Optional Sub-header (Tabs etc.) -->
          <div v-if="$slots.subheader" class="border-b border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950/50 shrink-0">
            <slot name="subheader"></slot>
          </div>

          <!-- Modal Body (Scrollable) -->
          <div class="flex-1 overflow-y-auto p-4 sm:p-5 overscroll-contain text-slate-800 dark:text-slate-200">
            <slot></slot>
          </div>

          <!-- Modal Footer -->
          <div v-if="$slots.footer" class="px-4 py-3 sm:px-5 sm:py-3.5 bg-slate-50 dark:bg-slate-950/90 border-t border-slate-200 dark:border-slate-800/80 sticky bottom-0 z-10 shrink-0 safe-area-pb">
            <slot name="footer"></slot>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, watch, nextTick } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    subtitle?: string
    icon?: string
    maxWidth?: 'sm' | 'md' | 'lg' | 'xl' | '2xl'
    closeOnBackdrop?: boolean
  }>(),
  {
    title: '',
    subtitle: '',
    icon: '',
    maxWidth: 'lg',
    closeOnBackdrop: true,
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'close'): void
}>()

const maxWidthClass = computed(() => {
  switch (props.maxWidth) {
    case 'sm': return 'sm:max-w-sm'
    case 'md': return 'sm:max-w-md'
    case 'lg': return 'sm:max-w-lg'
    case 'xl': return 'sm:max-w-xl'
    case '2xl': return 'sm:max-w-2xl'
    default: return 'sm:max-w-lg'
  }
})

const close = () => {
  emit('update:modelValue', false)
  emit('close')
}

const onBackdropClick = () => {
  if (props.closeOnBackdrop) {
    close()
  }
}

// Prevent background page scrolling when modal is open
watch(
  () => props.modelValue,
  (open) => {
    if (typeof document !== 'undefined') {
      if (open) {
        document.body.classList.add('overflow-hidden')
      } else {
        document.body.classList.remove('overflow-hidden')
      }
    }
  }
)
</script>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
  transform: scale(0.98);
}

.safe-area-pb {
  padding-bottom: max(0.75rem, env(safe-area-inset-bottom, 0.75rem));
}
</style>
