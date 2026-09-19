<template>
  <div class="min-h-screen flex flex-col pb-20 md:pb-6 font-sans antialiased text-slate-800 bg-slate-100 dark:text-slate-100 dark:bg-slate-900 selection:bg-emerald-500 selection:text-slate-950">
    <!-- Header -->
    <AppHeader />

    <!-- Stats Counter Banner -->
    <StatsBanner />

    <!-- Main View Content -->
    <main class="flex-1 max-w-7xl w-full mx-auto px-3 sm:px-4 py-4 sm:py-6">
      <!-- Catalog View -->
      <CatalogView v-if="uiStore.currentView === 'catalog'" />

      <!-- AI Queue View -->
      <QueueView v-if="uiStore.currentView === 'queue'" />

      <!-- Settings View -->
      <SettingsView v-if="uiStore.currentView === 'settings'" />
    </main>

    <!-- Mobile Bottom Navigation -->
    <MobileNav />

    <!-- Modals -->
    <UploadModal />
    <DetailModal />

    <!-- Toast Notification -->
    <ToastNotification />
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { createIcons, icons } from 'lucide'
import { useUiStore } from '@/stores/ui'
import { useCatalogStore } from '@/stores/catalog'
import { useQueueStore } from '@/stores/queue'
import { useSettingsStore } from '@/stores/settings'

import AppHeader from '@/components/layout/AppHeader.vue'
import StatsBanner from '@/components/layout/StatsBanner.vue'
import MobileNav from '@/components/layout/MobileNav.vue'

import CatalogView from '@/components/catalog/CatalogView.vue'
import QueueView from '@/components/queue/QueueView.vue'
import SettingsView from '@/components/settings/SettingsView.vue'

import UploadModal from '@/components/modals/UploadModal.vue'
import DetailModal from '@/components/modals/DetailModal.vue'
import ToastNotification from '@/components/common/ToastNotification.vue'

const uiStore = useUiStore()
const catalogStore = useCatalogStore()
const queueStore = useQueueStore()
const settingsStore = useSettingsStore()
const { locale } = useI18n()

const updateIcons = () => {
  nextTick(() => {
    try {
      createIcons({ icons })
    } catch (e) {
      console.error('Lucide icons render error:', e)
    }
  })
}

onMounted(() => {
  uiStore.initTheme()
  settingsStore.fetchSettings()
  catalogStore.fetchComponents()
  catalogStore.fetchDictionary()
  queueStore.fetchQueue()
  queueStore.startQueuePolling()
  updateIcons()
})

// Watch state changes to re-trigger icon creation when components or modals appear
watch(
  () => [
    locale.value,
    uiStore.isDarkMode,
    uiStore.currentView,
    uiStore.showUploadModal,
    uiStore.showDetailModal,
    catalogStore.filteredComponents.length,
    queueStore.aiQueue.length,
    uiStore.toastMessage,
  ],
  () => {
    updateIcons()
  },
  { deep: true }
)
</script>
