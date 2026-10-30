
import * as mock from './mockApi'
import type { Meta, RecommendRequest, RecommendResponse } from '../types'

const USE_MOCKS = (import.meta.env.VITE_USE_MOCKS ?? 'true') === 'true'

export const isMockMode = USE_MOCKS

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api${path}`, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new ApiError(response.status, problem?.detail ?? problem?.title ?? 'Erro ao falar com o servidor')
  }
  return response.json() as Promise<T>
}

export function getMeta(): Promise<Meta> {
  return USE_MOCKS ? mock.getMeta() : request<Meta>('/meta')
}

export function recommend(body: RecommendRequest): Promise<RecommendResponse> {
  return USE_MOCKS
    ? mock.recommend(body)
    : request<RecommendResponse>('/recommend', { method: 'POST', body: JSON.stringify(body) })
}
