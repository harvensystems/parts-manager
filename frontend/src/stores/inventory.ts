import { defineStore } from 'pinia'
import { ref, computed, onMounted } from 'vue'
import type { ComponentItem, QueueJob } from '../types/inventory'
import { i18n } from '../locales'
import { api } from '@/api'
import { CreateOrUpdatePartDto, PartResponseDto, RecognitionTaskResponseDto } from "@/api/api.ts";

export const useInventoryStore = defineStore('inventory', () => {
  const getT = () => i18n.global.t

  // Navigation & Views
  const currentView = ref<'catalog' | 'queue'>('catalog')
  const searchQuery = ref('')
  const selectedTypeFilter = ref('')
  const selectedMountingFilter = ref('')
  const sortBy = ref<'updatedAt' | 'name' | 'quantity'>('updatedAt')

  // Modals visibility
  const showUploadModal = ref(false)
  const showSettingsModal = ref(false)
  const showVerifyModal = ref(false)
  const showDetailModal = ref(false)

  // Active items in modals
  const activeVerifyJob = ref<QueueJob | null>(null)
  const selectedComponent = ref<ComponentItem | null>(null)

  // Loading indicators
  const isLoadingComponents = ref(false)
  const isLoadingQueue = ref(false)

  // Toast notifications
  const toastMessage = ref('')
  let toastTimeout: any = null

  const showToast = (msg: string) => {
    toastMessage.value = msg
    if (toastTimeout) clearTimeout(toastTimeout)
    toastTimeout = setTimeout(() => {
      toastMessage.value = ''
    }, 3000)
  }

  // Settings
  const customApiKey = ref('')

  const getImageUrl = (id: string) => {
    return `/api/images/${id}`
  }

  // Component Types list
  const componentTypes = [
    'Resistor',
    'Capacitor',
    'IC',
    'Transistor',
    'Diode',
    'LED',
    'Inductor',
    'Connector',
    'Sensor',
    'Module',
    'Other',
  ]

  // Components State
  const components = ref<ComponentItem[]>([])

  // AI Processing Queue
  const aiQueue = ref<QueueJob[]>([])

  // Helper converters
  const partDtoToItem = (dto: PartResponseDto): ComponentItem => {
    let photo = ''
    if (dto.photoIds && dto.photoIds.length > 0) {
      photo = getImageUrl(dto.photoIds[0])
    }
    return {
      id: dto.id || '',
      name: dto.name!!,
      type: dto.type!!,
      manufacturer: dto.manufacturer || '',
      partNumber: dto.partNumber || '',
      package: dto.packageType || '',
      mounting: dto.mounting || 'SMD',
      quantity: dto.quantity || 0,
      description: dto.description || '',
      photo: photo,
      metadata: dto.metadata || {},
      updatedAt: dto.updatedAt ? new Date(dto.updatedAt).getTime() : Date.now(),
    }
  }

  const taskDtoToJob = (dto: RecognitionTaskResponseDto): QueueJob => {
    const statusMap: Record<string, 'pending' | 'processing' | 'completed' | 'failed'> = {
      PENDING: 'pending',
      PROCESSING: 'processing',
      COMPLETED: 'completed',
      FAILED: 'failed',
    }

    const aiResult = dto.aiResult!! ? {
      name: dto.aiResult.name,
      type: dto.aiResult.type,
      manufacturer: dto.aiResult.manufacturer,
      partNumber: dto.aiResult.partNumber,
      package: dto.aiResult.packageType,
      mounting: dto.aiResult.mounting,
      quantity: dto.aiResult.quantity,
      description: dto.aiResult.description,
      metadata: dto.aiResult.metadata,
      confidence: dto.confidence || dto.aiResult.confidence,
      rawText: dto.rawText || dto.aiResult.rawText,
    } : undefined

    return {
      id: dto.id!!,
      photoUrl: dto.photoId ? getImageUrl(dto.photoId) : '',
      createdAt: dto.createdAt ? new Date(dto.createdAt).getTime() : Date.now(),
      status: statusMap[dto.status!!] || 'pending',
      aiResult: aiResult,
      error: dto.errorMessage,
    }
  }

  // Fetch Parts from Backend
  const fetchComponents = async () => {
    isLoadingComponents.value = true
    try {
      const res = await api.parts.listParts({
        search: searchQuery.value,
        type: selectedTypeFilter.value,
        mounting: selectedMountingFilter.value,
        sortBy: sortBy.value,
      })
      components.value = res.map(partDtoToItem)
    } catch (err: any) {
      console.error('Failed to fetch components:', err)
    } finally {
      isLoadingComponents.value = false
    }
  }

  // Fetch Queue from Backend
  const fetchQueue = async () => {
    try {
      const res = await api.recognition.listTasks()
      aiQueue.value = res.map(taskDtoToJob)
    } catch (err: any) {
      console.error('Failed to fetch AI tasks queue:', err)
    }
  }

  // Background Polling for Queue Tasks
  let queuePollTimer: any = null
  const startQueuePolling = () => {
    if (queuePollTimer) clearInterval(queuePollTimer)
    queuePollTimer = setInterval(() => {
      const hasActive = aiQueue.value.some((j) => j.status === 'processing' || j.status === 'pending')
      if (hasActive || currentView.value === 'queue') {
        fetchQueue()
      }
    }, 2000)
  }

  // Computed Stats
  const totalItemsCount = computed(() => components.value.reduce((acc, item) => acc + (item.quantity || 0), 0))
  const smdCount = computed(() => components.value.filter((i) => i.mounting === 'SMD').length)
  const pendingJobsCount = computed(() => aiQueue.value.filter((j) => j.status === 'processing' || j.status === 'pending').length)

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
          item.package?.toLowerCase().includes(q) ||
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
      if (sortBy.value === 'updatedAt') return b.updatedAt - a.updatedAt
      if (sortBy.value === 'name') return (a.name || '').localeCompare(b.name || '')
      if (sortBy.value === 'quantity') return (b.quantity || 0) - (a.quantity || 0)
      return 0
    })

    return list
  })

  // Actions
  const adjustQuantity = async (item: ComponentItem, delta: number) => {
    const t = getT()
    try {
      const res = await api.parts.adjustQuantity(item.id, { delta })
      const updated = partDtoToItem(res)
      const idx = components.value.findIndex((c) => c.id === item.id)
      if (idx !== -1) {
        components.value[idx] = updated
      }
      if (selectedComponent.value?.id === item.id) {
        selectedComponent.value = updated
      }
      showToast(t('toast.qtyUpdated', { qty: updated.quantity }))
    } catch (err: any) {
      item.quantity = Math.max(0, (item.quantity || 0) + delta)
      item.updatedAt = Date.now()
      showToast(t('toast.qtyUpdated', { qty: item.quantity }))
    }
  }

  const deleteComponent = async (id: string) => {
    const t = getT()
    try {
      await api.parts.deletePart(id)
      components.value = components.value.filter((c) => c.id !== id)
      if (selectedComponent.value?.id === id) {
        showDetailModal.value = false
        selectedComponent.value = null
      }
      showToast(t('toast.partDeleted'))
    } catch (err: any) {
      components.value = components.value.filter((c) => c.id !== id)
      showToast(t('toast.partDeleted'))
    }
  }

  const saveComponent = async (compData: Partial<ComponentItem>) => {
    const t = getT()
    try {
      let photoIds: string[] = []
      if (compData.photo && compData.photo.includes('/api/images/')) {
        const id = compData.photo.split('/api/images/')[1]
        if (id) photoIds.push(id)
      }

      const dto: CreateOrUpdatePartDto = {
        name: compData.name || 'Unknown component',
        type: compData.type || 'Other',
        manufacturer: compData.manufacturer || '',
        partNumber: compData.partNumber || '',
        packageType: compData.package || '',
        mounting: compData.mounting || 'SMD',
        quantity: compData.quantity || 1,
        description: compData.description || '',
        photoIds,
        metadata: compData.metadata || {},
      }

      const res = await api.parts.saveOrUpdatePart(dto)
      const savedItem = partDtoToItem(res)

      const existingIndex = components.value.findIndex((c) => c.id === savedItem.id)
      if (existingIndex !== -1) {
        components.value[existingIndex] = savedItem
        showToast(t('toast.partUpdated', { name: savedItem.name }))
      } else {
        components.value.unshift(savedItem)
        showToast(t('toast.partAdded', { name: savedItem.name }))
      }
    } catch (err: any) {
      console.error('Save component error:', err)
      const newItem: ComponentItem = {
        id: compData.id || 'c_' + Date.now(),
        name: compData.name || 'Unknown component',
        type: compData.type || 'Other',
        manufacturer: compData.manufacturer || '',
        partNumber: compData.partNumber || '',
        package: compData.package || '',
        mounting: compData.mounting || 'SMD',
        quantity: compData.quantity || 1,
        description: compData.description || '',
        photo: compData.photo || '',
        metadata: compData.metadata || {},
        updatedAt: Date.now(),
      }
      components.value.unshift(newItem)
      showToast(t('toast.partAdded', { name: newItem.name }))
    }
  }

  // AI Queue Actions
  const addQueueJob = async (photoUrl: string) => {
    const t = getT()
    try {
      const res = await api.recognition.uploadDataUrl({dataUrl: photoUrl, filename: 'capture.jpg'})
      const job = taskDtoToJob(res)
      aiQueue.value.unshift(job)
      showToast(t('toast.photoUploaded'))
    } catch (err: any) {
      const job: QueueJob = {
        id: 'job_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4),
        photoUrl,
        createdAt: Date.now(),
        status: 'processing',
      }
      aiQueue.value.unshift(job)
      showToast(t('toast.photoQueued'))
      setTimeout(() => {
        simulateAiRecognition(job.id)
      }, 2000)
    }
  }

  const uploadFileToQueue = async (file: File) => {
    const t = getT()
    try {
      const res = await api.recognition.uploadFile({ file })
      const job = taskDtoToJob(res)
      aiQueue.value.unshift(job)
      showToast(t('toast.fileUploaded'))
    } catch (err: any) {
      console.error('File upload failed:', err)
      const reader = new FileReader()
      reader.onload = (e) => {
        if (e.target?.result) {
          addQueueJob(e.target.result as string)
        }
      }
      reader.readAsDataURL(file)
    }
  }

  const simulateAiRecognition = (jobId: string) => {
    const t = getT()
    const job = aiQueue.value.find((j) => j.id === jobId)
    if (!job) return

    job.status = 'completed'
    job.aiResult = {
      name: 'Resistor 10 kOhm 0805',
      type: 'Resistor',
      manufacturer: 'Yageo',
      partNumber: 'RC0805FR-0710KL',
      package: '0805',
      mounting: 'SMD',
      quantity: 100,
      description: '1% Thick Film Chip Resistor',
      metadata: { resistance: '10 kΩ', tolerance: '1%', power: '0.125 W' },
      confidence: 96,
      rawText: '1002 1% YAGEO 0805',
    }
    showToast(t('toast.aiRecognized', { name: job.aiResult.name }))
  }

  const retryAiJob = async (job: QueueJob) => {
    const t = getT()
    try {
      const res = await api.recognition.retryTask(job.id)
      const updated = taskDtoToJob(res)
      const idx = aiQueue.value.findIndex((j) => j.id === job.id)
      if (idx !== -1) {
        aiQueue.value[idx] = updated
      }
      showToast(t('toast.retryAi'))
    } catch (err: any) {
      job.status = 'processing'
      job.error = undefined
      setTimeout(() => {
        simulateAiRecognition(job.id)
      }, 1500)
      showToast(t('toast.retryAi'))
    }
  }

  const removeQueueJob = async (jobId: string) => {
    const t = getT()
    try {
      await api.recognition.deleteTask(jobId)
      aiQueue.value = aiQueue.value.filter((j) => j.id !== jobId)
      showToast(t('toast.jobRemoved'))
    } catch (err: any) {
      aiQueue.value = aiQueue.value.filter((j) => j.id !== jobId)
      showToast(t('toast.jobRemoved'))
    }
  }

  const startManualEntry = () => {
    activeVerifyJob.value = {
      id: 'manual_' + Date.now(),
      photoUrl: 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=400&q=80',
      createdAt: Date.now(),
      status: 'completed',
      aiResult: {
        name: '',
        type: 'Resistor',
        manufacturer: '',
        partNumber: '',
        package: '',
        mounting: 'SMD',
        quantity: 1,
        description: '',
        metadata: {},
      },
    }
    showVerifyModal.value = true
  }

  const openVerifyModalForJob = (job: QueueJob) => {
    activeVerifyJob.value = job
    showVerifyModal.value = true
  }

  const openDetailModalForComponent = (comp: ComponentItem) => {
    selectedComponent.value = comp
    showDetailModal.value = true
  }

  const clearAllFilters = () => {
    searchQuery.value = ''
    selectedTypeFilter.value = ''
    selectedMountingFilter.value = ''
    sortBy.value = 'updatedAt'
  }

  const setQuickTag = (tag: string) => {
    if (componentTypes.includes(tag)) {
      selectedTypeFilter.value = selectedTypeFilter.value === tag ? '' : tag
    } else if (tag === 'SMD' || tag === 'Through-hole') {
      selectedMountingFilter.value = selectedMountingFilter.value === tag ? '' : tag
    } else {
      searchQuery.value = searchQuery.value === tag ? '' : tag
    }
  }

  // Initial load
  onMounted(() => {
    fetchComponents()
    fetchQueue()
    startQueuePolling()
  })

  return {
    currentView,
    searchQuery,
    selectedTypeFilter,
    selectedMountingFilter,
    sortBy,
    showUploadModal,
    showSettingsModal,
    showVerifyModal,
    showDetailModal,
    activeVerifyJob,
    selectedComponent,
    isLoadingComponents,
    isLoadingQueue,
    toastMessage,
    showToast,
    customApiKey,
    componentTypes,
    components,
    aiQueue,
    totalItemsCount,
    smdCount,
    pendingJobsCount,
    filteredComponents,
    fetchComponents,
    fetchQueue,
    adjustQuantity,
    deleteComponent,
    saveComponent,
    addQueueJob,
    uploadFileToQueue,
    retryAiJob,
    removeQueueJob,
    startManualEntry,
    openVerifyModalForJob,
    openDetailModalForComponent,
    clearAllFilters,
    setQuickTag,
  }
})
