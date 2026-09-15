<template>
  <div class="min-h-screen flex flex-col pb-20 md:pb-6 font-sans antialiased text-slate-100 bg-slate-950 selection:bg-emerald-500 selection:text-slate-950">
    <!-- Header -->
    <AppHeader />

    <!-- Stats Counter Banner -->
    <StatsBanner />

    <!-- Main View Content -->
    <main class="flex-1 max-w-7xl w-full mx-auto px-3 sm:px-4 py-4 sm:py-6">
      <!-- Catalog View -->
      <CatalogView v-if="store.currentView === 'catalog'" />

      <!-- AI Queue View -->
      <QueueView v-if="store.currentView === 'queue'" />
    </main>

    <!-- Mobile Bottom Navigation -->
    <MobileNav />

    <!-- Modals -->
    <UploadModal />
    <VerifyModal />
    <DetailModal />
    <SettingsModal />

    <!-- Toast Notification -->
    <ToastNotification />
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { createIcons, icons } from 'lucide'
import { useInventoryStore } from './stores/inventory'

import AppHeader from './components/layout/AppHeader.vue'
import StatsBanner from './components/layout/StatsBanner.vue'
import MobileNav from './components/layout/MobileNav.vue'

import CatalogView from './components/catalog/CatalogView.vue'
import QueueView from './components/queue/QueueView.vue'

import UploadModal from './components/modals/UploadModal.vue'
import VerifyModal from './components/modals/VerifyModal.vue'
import DetailModal from './components/modals/DetailModal.vue'
import SettingsModal from './components/modals/SettingsModal.vue'
import ToastNotification from './components/common/ToastNotification.vue'

const store = useInventoryStore()
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
  updateIcons()
})

// Watch state changes to re-trigger icon creation when components or modals appear
watch(
  () => [
    locale.value,
    store.currentView,
    store.showUploadModal,
    store.showVerifyModal,
    store.showDetailModal,
    store.showSettingsModal,
    store.filteredComponents.length,
    store.aiQueue.length,
    store.toastMessage,
  ],
  () => {
    updateIcons()
  },
  { deep: true }
)
</script>
