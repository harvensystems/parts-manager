import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '@/api'
import type {CreateOrUpdatePartDto, DictionaryResponseDto, PartResponseDto} from '@/api/api'
import { useUiStore } from '@/stores/ui'
import { i18n } from '@/locales'

export const useCatalogStore = defineStore('catalog', () => {
  const uiStore = useUiStore()
  const getT = () => i18n.global.t

  // Search & Filters
  const searchQuery = ref('')
  const selectedTypeFilter = ref('')
  const selectedMountingFilter = ref('')
  const sortBy = ref<'updatedAt' | 'name' | 'quantity'>('updatedAt')

  // Components state
  const components = ref<PartResponseDto[]>([])
  const component = ref<CreateOrUpdatePartDto>({} as CreateOrUpdatePartDto)
  const dictionary = ref<DictionaryResponseDto>({})
  const isLoadingComponents = ref(false)
  const isLoadingDictionary = ref(false)
  const selectedComponent = ref<PartResponseDto>({} as PartResponseDto)

  // Computed Stats
  const totalItemsCount = computed(() =>
    components.value.reduce((acc, item) => acc + (item.quantity || 0), 0)
  )
  const smdCount = computed(() =>
    components.value.filter((i) => i.mounting === 'SMD').length
  )

  const getImageUrl = (id: string | string[] | undefined) => {
    if (!id) return 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400&q=80'
    let end = ''
    if (typeof id === 'string') end = id
    else end = id[0]
    return `/api/images/${end}`
  }

  // Filtered & Sorted Components
  const filteredComponents = computed(() => {
    let list = [...components.value]

    if (searchQuery.value.trim()) {
      const q = searchQuery.value.toLowerCase().trim()
      list = list.filter((item) => {
        const basicMatch =
          item.name?.toLowerCase().includes(q) ||
          item.partNumber?.toLowerCase().includes(q) ||
          item.type?.toLowerCase().includes(q) ||
          item.manufacturer?.toLowerCase().includes(q) ||
          item.packageType?.toLowerCase().includes(q) ||
          item.mounting?.toLowerCase().includes(q) ||
          item.description?.toLowerCase().includes(q)

        const metaMatch =
          item.metadata &&
          Object.entries(item.metadata).some(
            ([k, v]) => k.toLowerCase().includes(q) || String(v).toLowerCase().includes(q)
          )

        return basicMatch || metaMatch
      })
    }

    if (selectedTypeFilter.value) {
      list = list.filter((i) => i.type === selectedTypeFilter.value)
    }

    if (selectedMountingFilter.value) {
      list = list.filter((i) => i.mounting === selectedMountingFilter.value)
    }

    list.sort((a, b) => {
      if (sortBy.value === 'updatedAt') return (b.updatedAt ?? 0) - (a.updatedAt ?? 0)
      if (sortBy.value === 'name') return (a.name || '').localeCompare(b.name || '')
      if (sortBy.value === 'quantity') return (b.quantity || 0) - (a.quantity || 0)
      return 0
    })

    return list
  })

  const fetchDictionary = async () => {
    isLoadingDictionary.value = true
    try {
      const res = await api.parts.getDictionary()
      dictionary.value = res
    } catch (err: any) {
      console.error('Failed to fetch dictionary:', err)
    } finally {
      isLoadingDictionary.value = false
    }
  }

  // Actions
  const fetchComponents = async () => {
    isLoadingComponents.value = true
    try {
      components.value = await api.parts.listParts({
        search: searchQuery.value,
        type: selectedTypeFilter.value,
        mounting: selectedMountingFilter.value,
        sortBy: sortBy.value,
      })
    } catch (err: any) {
      console.error('Failed to fetch components:', err)
    } finally {
      isLoadingComponents.value = false
    }
  }

  const adjustQuantity = async (item: PartResponseDto, delta: number) => {
    const t = getT()
    try {
      const res = await api.parts.adjustQuantity(item.id!!, { delta })
      const idx = components.value.findIndex((c) => c.id === item.id)
      if (idx !== -1) {
        components.value[idx] = res
      }
      if (selectedComponent.value?.id === item.id) {
        selectedComponent.value = res
      }
      uiStore.showToast(t('toast.qtyUpdated', { qty: res.quantity }))
    } catch (err: any) {
      console.error('Failed to adjust quantity on server:', err)
    }
  }

  const deleteComponent = async (id: string) => {
    const t = getT()
    try {
      await api.parts.deletePart(id)
      components.value = components.value.filter((c) => c.id !== id)
      if (selectedComponent.value?.id === id) {
        uiStore.showDetailModal = false
        selectedComponent.value = { }
      }
      uiStore.showToast(t('toast.partDeleted'))
    } catch (err: any) {
      console.error('Failed to delete part on server:', err)
      components.value = components.value.filter((c) => c.id !== id)
      uiStore.showToast(t('toast.partDeleted'))
    }
  }

  const saveComponent = async (compData: CreateOrUpdatePartDto) => {
    const t = getT()
    try {
      const res = await api.parts.saveOrUpdatePart(compData)

      const existingIndex = components.value.findIndex((c) => c.id === res.id)
      if (existingIndex !== -1) {
        components.value[existingIndex] = res
        uiStore.showToast(t('toast.partUpdated', { name: res.name }))
      } else {
        components.value.unshift(res)
        uiStore.showToast(t('toast.partAdded', { name: res.name }))
      }
    } catch (err: any) {
      console.error('Save component error:', err)
    }
  }

  const openDetailModalForComponent = (comp: PartResponseDto) => {
    selectedComponent.value = comp
    uiStore.showDetailModal = true
  }

  const clearAllFilters = () => {
    searchQuery.value = ''
    selectedTypeFilter.value = ''
    selectedMountingFilter.value = ''
    sortBy.value = 'updatedAt'
  }

  return {
    searchQuery,
    selectedTypeFilter,
    selectedMountingFilter,
    sortBy,
    components,
    component,
    dictionary,
    isLoadingComponents,
    isLoadingDictionary,
    selectedComponent,
    totalItemsCount,
    smdCount,
    getImageUrl,
    filteredComponents,
    fetchDictionary,
    fetchComponents,
    adjustQuantity,
    deleteComponent,
    saveComponent,
    openDetailModalForComponent,
    clearAllFilters,
  }
})
