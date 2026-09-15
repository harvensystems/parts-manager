import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface PartDto {
  id?: string
  name: string
  type: string
  manufacturer?: string
  partNumber?: string
  packageType?: string
  mounting?: string
  quantity: number
  description?: string
  photoIds?: string[]
  metadata?: Record<string, any>
  createdAt?: string
  updatedAt?: string
}

export interface RecognitionTaskDto {
  id: string
  photoId: string
  originalFilename?: string
  contentType?: string
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
  aiResult?: {
    name?: string
    type?: string
    manufacturer?: string
    partNumber?: string
    packageType?: string
    mounting?: string
    quantity?: number
    description?: string
    metadata?: Record<string, any>
    confidence?: number
    rawText?: string
  }
  rawText?: string
  confidence?: number
  errorMessage?: string
  processingTimeMs?: number
  createdAt?: string
  completedAt?: string
}

export const PartsApi = {
  list(params?: { search?: string; type?: string; mounting?: string; sortBy?: string }) {
    return api.get<PartDto[]>('/parts', { params })
  },
  getById(id: string) {
    return api.get<PartDto>(`/parts/${id}`)
  },
  saveOrUpdate(data: Partial<PartDto>) {
    return api.post<PartDto>('/parts', data)
  },
  update(id: string, data: Partial<PartDto>) {
    return api.put<PartDto>(`/parts/${id}`, data)
  },
  adjustQuantity(id: string, delta: number) {
    return api.patch<PartDto>(`/parts/${id}/quantity`, { delta })
  },
  delete(id: string) {
    return api.delete(`/parts/${id}`)
  },
}

export const RecognitionApi = {
  listTasks() {
    return api.get<RecognitionTaskDto[]>('/recognition-tasks')
  },
  getTask(id: string) {
    return api.get<RecognitionTaskDto>(`/recognition-tasks/${id}`)
  },
  uploadFile(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<RecognitionTaskDto>('/recognition-tasks', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },
  uploadDataUrl(dataUrl: string, filename?: string) {
    return api.post<RecognitionTaskDto>('/recognition-tasks/data-url', { dataUrl, filename })
  },
  retry(id: string) {
    return api.post<RecognitionTaskDto>(`/recognition-tasks/${id}/retry`)
  },
  deleteTask(id: string) {
    return api.delete(`/recognition-tasks/${id}`)
  },
}

export const ImagesApi = {
  getImageUrl(id: string) {
    return `/api/images/${id}`
  },
  upload(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<{ id: string; url: string }>('/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },
  delete(id: string) {
    return api.delete(`/images/${id}`)
  },
}

export default api
