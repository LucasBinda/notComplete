// Backend falso até os microsserviços ficarem prontos. Não interpreta o texto livre.

import catalog from '../mocks/catalog.json'
import { shortProviderName } from '../lib/format'
import type { Meta, Recommendation, RecommendRequest, RecommendResponse } from '../types'

const LATENCY_MS = 700

const titles = catalog.titles as Recommendation[]

function wait<T>(value: T): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(value), LATENCY_MS))
}

export function getMeta(): Promise<Meta> {
  return wait(catalog.meta as Meta)
}

function exampleReason(title: Recommendation, providerName: string): string {
  const genres = (title.genres ?? []).slice(0, 2).map((g) => g.name.toLowerCase()).join(' e ')
  const length =
    title.mediaType === 'movie' && title.runtime
      ? title.runtime <= 120
        ? 'cabe numa sessão de até 2h'
        : 'um programa mais longo'
      : 'série para maratonar'
  return `Mistura de ${genres}, ${length} e disponível em ${shortProviderName(providerName)}.`
}

export function recommend(request: RecommendRequest): Promise<RecommendResponse> {
  const filters = request.filters ?? {}
  const wanted = new Set(request.providers)
  const page = request.page ?? 1
  const limit = request.limit ?? 10

  const matches = titles
    .filter((t) => t.watch.providers.some((p) => wanted.has(p.id)))
    .filter((t) => !filters.mediaType || t.mediaType === filters.mediaType)
    .filter((t) => !filters.runtimeMax || t.mediaType === 'tv' || (t.runtime ?? 0) <= filters.runtimeMax)
    .filter((t) => !filters.genres?.length || t.genres?.some((g) => filters.genres!.includes(g.id)))

  const items = matches
    .slice((page - 1) * limit, page * limit)
    .map((t) => {
      const providers = t.watch.providers.filter((p) => wanted.has(p.id))
      return { ...t, watch: { ...t.watch, providers }, reason: exampleReason(t, providers[0].name) }
    })

  const sources: Record<string, 'filter'> = {}
  for (const key of Object.keys(filters)) sources[key] = 'filter'

  const notices = ['Modo demonstração: títulos reais do TMDB, mas a IA ainda não está conectada.']
  if (request.query) notices.push('O texto livre só será interpretado quando o llm-service estiver pronto.')
  if (matches.length === 0) notices.push('Nenhum título de exemplo combina com esses filtros.')

  return wait({
    requestId: `mock-${Date.now()}`,
    criteria: { ...filters, providers: request.providers, summary: null, sources },
    items,
    page,
    hasMore: page * limit < matches.length,
    degraded: false,
    notices,
  })
}
