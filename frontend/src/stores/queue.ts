import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { QueueJob } from '@/types/inventory'
import { api } from '@/api'
import type { CreateOrUpdatePartDto, RecognitionTaskResponseDto } from '@/api/api'
import { useUiStore } from '@/stores/ui'
import { i18n } from '@/locales'
import { useCatalogStore } from '@/stores/catalog.ts'

export const useQueueStore = defineStore('queue', () => {
  const uiStore = useUiStore()
  const catalogStore = useCatalogStore()
  const getT = () => i18n.global.t

  const aiQueue = ref<RecognitionTaskResponseDto[]>([])
  const isLoadingQueue = ref(false)
  const activeVerifyJob = ref<QueueJob | null>(null)

  const pendingJobsCount = computed(() =>
    aiQueue.value.filter((j) => j.status === 'PROCESSING' || j.status === 'PENDING').length
  )

  const fetchQueue = async () => {
    isLoadingQueue.value = true
    try {
      aiQueue.value = await api.recognition.listTasks()
      // aiQueue.value = res.map(taskDtoToJob)
    } catch (err: any) {
      console.error('Failed to fetch AI tasks queue:', err)
    } finally {
      isLoadingQueue.value = false
    }
  }

  let queuePollTimer: any = null
  const startQueuePolling = () => {
    if (queuePollTimer) clearInterval(queuePollTimer)
    queuePollTimer = setInterval(() => {
      const hasActive = aiQueue.value.some((j) => j.status === 'PROCESSING' || j.status === 'PENDING')
      if (hasActive || uiStore.currentView === 'queue') {
        fetchQueue()
      }
    }, 2000)
  }

  const addQueueJob = async (photoUrl: string) => {
    const t = getT()
    try {
      const res = await api.recognition.uploadDataUrl({ dataUrl: photoUrl, filename: 'capture.jpg' })
      aiQueue.value.unshift(res)
      uiStore.showToast(t('toast.photoUploaded'))
    } catch (err: any) {
      console.error('Failed to upload dataUrl to recognition queue:', err)
      uiStore.showToast(t('toast.photoUploaded'))
    }
  }

  const uploadFileToQueue = async (file: File) => {
    const t = getT()
    try {
      const res = await api.recognition.uploadFile({ file })
      aiQueue.value.unshift(res)
      uiStore.showToast(t('toast.fileUploaded'))
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

  const retryAiJob = async (job: RecognitionTaskResponseDto) => {
    const t = getT()
    try {
      const res = await api.recognition.retryTask(job.id!!)
      const idx = aiQueue.value.findIndex((j) => j.id === job.id)
      if (idx !== -1) {
        aiQueue.value[idx] = res
      }
      uiStore.showToast(t('toast.retryAi'))
    } catch (err: any) {
      console.error('Failed to retry AI recognition:', err)
      job.status = 'PROCESSING'
      uiStore.showToast(t('toast.retryAi'))
    }
  }

  const removeQueueJob = async (jobId: string) => {
    const t = getT()
    try {
      await api.recognition.deleteTask(jobId)
      aiQueue.value = aiQueue.value.filter((j) => j.id !== jobId)
      uiStore.showToast(t('toast.jobRemoved'))
    } catch (err: any) {
      console.error('Failed to remove AI queue job:', err)
      aiQueue.value = aiQueue.value.filter((j) => j.id !== jobId)
      uiStore.showToast(t('toast.jobRemoved'))
    }
  }

  const openVerifyModalForJob = (job: RecognitionTaskResponseDto) => {
    catalogStore.component = job.part as CreateOrUpdatePartDto
    uiStore.showUploadModal = true
  }

  return {
    aiQueue,
    isLoadingQueue,
    activeVerifyJob,
    pendingJobsCount,
    fetchQueue,
    startQueuePolling,
    addQueueJob,
    uploadFileToQueue,
    retryAiJob,
    removeQueueJob,
    openVerifyModalForJob,
  }
})
