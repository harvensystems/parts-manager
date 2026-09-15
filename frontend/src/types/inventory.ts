export interface ComponentItem {
  id: string
  name: string
  type: string
  manufacturer?: string
  partNumber?: string
  package?: string
  mounting?: 'SMD' | 'Through-hole' | string
  quantity: number
  description?: string
  photo?: string
  metadata: Record<string, any>
  updatedAt: number
}

export type QueueJobStatus = 'pending' | 'processing' | 'completed' | 'failed'

export interface QueueJob {
  id: string
  photoUrl: string
  createdAt: number
  status: QueueJobStatus
  aiResult?: {
    name?: string
    type?: string
    manufacturer?: string
    partNumber?: string
    package?: string
    mounting?: string
    quantity?: number
    description?: string
    metadata?: Record<string, any>
    confidence?: number
    rawText?: string
  }
  error?: string
}

export interface SampleImage {
  name: string
  url: string
  type: string
  partNumber: string
}
