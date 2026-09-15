<template>
  <BaseModal
    v-model="store.showSettingsModal"
    :title="t('settingsModal.title')"
    :subtitle="t('settingsModal.subtitle')"
    icon="sliders"
    maxWidth="md"
  >
    <div class="space-y-4 text-xs sm:text-sm">
      <div class="space-y-1">
        <label class="block text-xs font-semibold text-slate-300">{{ t('settingsModal.provider') }}</label>
        <select class="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500">
          <option value="openai">OpenAI (GPT-4o / GPT-4o-mini via Spring AI)</option>
          <option value="anthropic">Anthropic Claude 3.5 Sonnet</option>
          <option value="ollama">Local Ollama / LLaVA Vision</option>
        </select>
      </div>

      <div class="space-y-1">
        <label class="block text-xs font-semibold text-slate-300">{{ t('settingsModal.customKey') }}</label>
        <input 
          v-model="store.customApiKey" 
          type="password" 
          placeholder="sk-..."
          class="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-white font-mono text-xs focus:outline-none focus:border-emerald-500"
        >
        <p class="text-[11px] text-slate-500">{{ t('settingsModal.customKeyHelp') }}</p>
      </div>

      <div class="bg-slate-950 p-3 rounded-lg border border-slate-800 text-xs text-slate-400 space-y-1">
        <div class="font-semibold text-slate-300 flex items-center gap-1.5">
          <i data-lucide="info" class="w-4 h-4 text-emerald-400"></i>
          {{ t('settingsModal.howItWorksTitle') }}
        </div>
        <p>
          {{ t('settingsModal.howItWorksDesc') }}
        </p>
      </div>
    </div>

    <!-- Modal Footer -->
    <template #footer>
      <div class="flex justify-end">
        <button 
          @click="saveSettings" 
          class="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-slate-950 font-bold text-xs sm:text-sm rounded-lg transition cursor-pointer active:scale-95"
        >
          {{ t('settingsModal.save') }}
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

const saveSettings = () => {
  store.showSettingsModal = false
  store.showToast(t('settingsModal.savedToast'))
}
</script>
