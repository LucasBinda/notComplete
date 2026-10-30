import type { AppliedCriteria, Genre, Meta, WatchProvider } from '../types'

// 108 → "1h48min", 120 → "2h", 45 → "45min"
export function formatRuntime(minutes: number): string {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0) return `${m}min`
  return m === 0 ? `${h}h` : `${h}h${String(m).padStart(2, '0')}min`
}

export function formatOffer(offer: WatchProvider['offer']): string {
  return { flatrate: 'Assinatura', free: 'Grátis', ads: 'Grátis com anúncios', rent: 'Aluguel', buy: 'Compra' }[offer]
}

// Nomes do TMDB são longos ("Amazon Prime Video"); na interface usamos os nomes populares
const SHORT_NAMES: Record<string, string> = {
  'Amazon Prime Video': 'Prime Video',
  'Disney Plus': 'Disney+',
  'Paramount Plus': 'Paramount+',
}

export function shortProviderName(name: string): string {
  return SHORT_NAMES[name] ?? name
}

export function formatCertification(cert: string): string {
  return cert === 'L' ? 'Livre' : `${cert} anos`
}

export interface CriteriaChip {
  label: string
  source: 'filter' | 'ai'
}

export function criteriaChips(criteria: AppliedCriteria, meta: Meta): CriteriaChip[] {
  const genreName = (id: number) =>
    [...meta.genres.movie, ...meta.genres.tv].find((g: Genre) => g.id === id)?.name ?? `#${id}`
  const source = (key: string) => criteria.sources?.[key] ?? 'filter'
  const chips: CriteriaChip[] = []

  if (criteria.mediaType) {
    chips.push({ label: criteria.mediaType === 'movie' ? 'Filmes' : 'Séries', source: source('mediaType') })
  }
  for (const id of criteria.genres ?? []) chips.push({ label: genreName(id), source: source('genres') })
  for (const id of criteria.excludeGenres ?? []) {
    chips.push({ label: `sem ${genreName(id).toLowerCase()}`, source: source('excludeGenres') })
  }
  if (criteria.runtimeMax) {
    chips.push({ label: `até ${formatRuntime(criteria.runtimeMax)}`, source: source('runtimeMax') })
  }
  if (criteria.yearFrom || criteria.yearTo) {
    const label =
      criteria.yearFrom && criteria.yearTo
        ? `${criteria.yearFrom}–${criteria.yearTo}`
        : criteria.yearFrom
          ? `a partir de ${criteria.yearFrom}`
          : `até ${criteria.yearTo}`
    chips.push({ label, source: source('yearFrom') })
  }
  if (criteria.certificationMax) {
    chips.push({ label: `até ${formatCertification(criteria.certificationMax)}`, source: source('certificationMax') })
  }
  if (criteria.referenceTitle) {
    chips.push({ label: `parecido com ${criteria.referenceTitle}`, source: 'ai' })
  }
  for (const id of criteria.providers) {
    const name = meta.providers.find((p) => p.id === id)?.name
    if (name) chips.push({ label: shortProviderName(name), source: 'filter' })
  }
  return chips
}
