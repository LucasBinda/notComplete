import { formatRuntime } from './format'
import type { AppliedCriteria, Criteria, MediaType, Meta } from '../types'

export type FilterField = 'genre' | 'runtimeMax' | 'mediaType'

export interface SearchForm {
  query: string
  genre: string // id do gênero ou '' para qualquer
  runtimeMax: string // minutos ou ''
  mediaType: string // 'movie' | 'tv' | ''
  providers: number[]
  // Campos que a IA preencheu na última busca. Não vão como filtro do usuário:
  // na próxima busca a IA decide de novo. Mexeu no campo, ele vira seu.
  aiFields: FilterField[]
}

export const RUNTIME_OPTIONS = [
  { value: '', label: 'Qualquer duração' },
  { value: '90', label: 'Até 1h30' },
  { value: '120', label: 'Até 2 horas' },
  { value: '150', label: 'Até 2h30' },
]

export const MEDIA_OPTIONS = [
  { value: '', label: 'Filme e série' },
  { value: 'movie', label: 'Só filmes' },
  { value: 'tv', label: 'Só séries' },
]

// A IA pode escolher uma duração fora da lista (ex.: 100 min); aí entra uma opção a mais
export function runtimeOptions(value: string) {
  if (RUNTIME_OPTIONS.some((o) => o.value === value)) return RUNTIME_OPTIONS
  return [...RUNTIME_OPTIONS, { value, label: runtimeLabel(value) }]
}

export function runtimeLabel(value: string): string {
  return RUNTIME_OPTIONS.find((o) => o.value === value)?.label ?? `Até ${formatRuntime(Number(value))}`
}

// Gêneros de série têm ids próprios no TMDB; sem formato escolhido, usamos os de filme
export function genresFor(mediaType: string, meta: Meta) {
  return mediaType === 'tv' ? meta.genres.tv : meta.genres.movie
}

// Mantém as plataformas: elas são do usuário, não da busca
export function clearSearch(form: SearchForm): SearchForm {
  return { ...form, query: '', genre: '', runtimeMax: '', mediaType: '', aiFields: [] }
}

// O usuário mudou um campo: ele deixa de ser da IA
export function setField(form: SearchForm, field: FilterField, value: string): SearchForm {
  const next = { ...form, [field]: value, aiFields: form.aiFields.filter((f) => f !== field) }
  // Trocar o formato troca a lista de gêneros
  if (field === 'mediaType') {
    next.genre = ''
    next.aiFields = next.aiFields.filter((f) => f !== 'genre')
  }
  return next
}

// Só o que o usuário escolheu vai como filtro; o que a IA preencheu fica de fora
export function userFilters(form: SearchForm): Criteria {
  const mine = (field: FilterField) => form[field] !== '' && !form.aiFields.includes(field)
  const filters: Criteria = {}
  if (mine('mediaType')) filters.mediaType = form.mediaType as MediaType
  if (mine('genre')) filters.genres = [Number(form.genre)]
  if (mine('runtimeMax')) filters.runtimeMax = Number(form.runtimeMax)
  return filters
}

// Copia para os campos da tela o que a IA entendeu na busca
export function applyAiCriteria(form: SearchForm, criteria: AppliedCriteria, meta: Meta): SearchForm {
  const fromAi = (key: string) => criteria.sources?.[key] === 'ai'
  // O que a IA tinha preenchido antes some; ela decidiu de novo agora
  const next: SearchForm = { ...form, aiFields: [] }
  for (const field of form.aiFields) next[field] = ''

  if (fromAi('mediaType') && criteria.mediaType) {
    // Gênero escolhido pelo usuário que não existe no novo formato: não troca o formato
    const keepsGenre = !next.genre || genresFor(criteria.mediaType, meta).some((g) => String(g.id) === next.genre)
    if (keepsGenre) {
      next.mediaType = criteria.mediaType
      next.aiFields.push('mediaType')
    }
  }
  if (fromAi('genres') && criteria.genres) {
    // A tela tem um gênero só: fica o primeiro que existe na lista do formato
    const ids = new Set(genresFor(next.mediaType, meta).map((g) => g.id))
    const id = criteria.genres.find((g) => ids.has(g))
    if (id !== undefined) {
      next.genre = String(id)
      next.aiFields.push('genre')
    }
  }
  if (fromAi('runtimeMax') && criteria.runtimeMax) {
    next.runtimeMax = String(criteria.runtimeMax)
    next.aiFields.push('runtimeMax')
  }
  return next
}
