<script setup lang="ts">
import { computed } from 'vue'
import { ElSelect, ElOption } from 'element-plus'

const props = withDefaults(defineProps<{
  label?: string,
  items: string[] | object
  placeholder?: string
  optionsKeyName?: string
  optionsValueName?: string
  size?: "small" | "default" | "large"
}>(), {
  optionsKeyName: 'name',
  optionsValueName: 'value',
  size: "default"
})

const model = defineModel<string>()

const list = computed(() => {
  if (Array.isArray(props.items)) {
    return props.items
  }
  return Object.entries(props.items).map(([key, value]) => ({
    [props.optionsKeyName]: key,
    [props.optionsValueName]: value
  }))
})

</script>

<template>
  <div class="space-y-1">
    <label v-if="label" class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ label }}</label>
    <el-select
        class="custom-select"
        :class="{ small: size === 'small' }"
        v-model="model"
        :placeholder="placeholder"
        :size="size"
        allow-create
        filterable
        default-first-option>
      <el-option
        v-for="item in list"
        :key="item"
        :label="item"
        :value="item"
      />
    </el-select>
  </div>
<!--  <div class="space-y-1">-->
<!--    <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300">{{ label }}</label>-->
<!--    <input-->
<!--      v-model="model"-->
<!--      list="custom-options"-->
<!--      :placeholder="placeholder"-->
<!--      class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"-->
<!--    />-->
<!--    <datalist id="custom-options">-->
<!--      <option v-for="name in list" :key="name" :value="name" />-->
<!--    </datalist>-->
<!--    <select-->
<!--        v-model="model"-->
<!--        placeholder="werfsdf"-->
<!--        class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"-->
<!--    >-->
<!--      <option v-for="name in list" :key="name" :value="name">-->
<!--        {{ name }}-->
<!--      </option>-->
<!--    </select>-->
<!--  </div>-->
</template>
<style scoped>
.custom-select :deep(.el-select__wrapper) {
  @apply px-3 py-1.5
  bg-slate-50 dark:bg-slate-950
  border border-slate-300 dark:border-slate-700
  rounded-lg
  text-slate-900 dark:text-white
  text-xs sm:text-sm
  shadow-none;
}
.custom-select.small :deep(.el-select__wrapper) {
  @apply py-1;
}

.custom-select :deep(.el-select__wrapper.is-focused) {
  @apply border-emerald-500;
}
</style>