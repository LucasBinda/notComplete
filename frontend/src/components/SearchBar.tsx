import { Search, SlidersHorizontal } from 'lucide-react'
import { shortProviderName } from '../lib/format'
import { MEDIA_OPTIONS, genresFor, runtimeLabel, type SearchForm } from '../lib/searchForm'
import type { Meta } from '../types'

interface Props {
  meta: Meta
  form: SearchForm
  expanded: boolean
  onOpen: () => void
}

function filterSummary(form: SearchForm, meta: Meta): string[] {
  const parts: string[] = []
  const genre = genresFor(form.mediaType, meta).find((g) => String(g.id) === form.genre)
  if (form.mediaType) parts.push(MEDIA_OPTIONS.find((o) => o.value === form.mediaType)!.label)
  if (genre) parts.push(genre.name)
  if (form.runtimeMax) parts.push(runtimeLabel(form.runtimeMax))

  const names = meta.providers.filter((p) => form.providers.includes(p.id)).map((p) => shortProviderName(p.name))
  if (names.length === 0) parts.push('Nenhuma plataforma')
  else parts.push(names.length <= 3 ? names.join(', ') : `${names.length} plataformas`)
  return parts
}

export function SearchBar({ meta, form, expanded, onOpen }: Props) {
  return (
    <button
      type="button"
      onClick={onOpen}
      aria-haspopup="dialog"
      aria-expanded={expanded}
      className="group flex w-full items-center gap-3 rounded-2xl border border-line bg-surface/80 px-4 py-3 text-left transition hover:border-brand/60 focus-visible:border-brand focus-visible:ring-2 focus-visible:ring-brand/30 focus-visible:outline-none sm:px-5 sm:py-4"
    >
      <Search className="size-5 shrink-0 text-brand-soft" />
      <span className="min-w-0 flex-1">
        <span className={`block truncate text-[15px] sm:text-base ${form.query ? 'text-ink' : 'text-faint'}`}>
          {form.query || 'Descreva o que quer assistir…'}
        </span>
        <span className="mt-0.5 block truncate text-xs text-muted sm:text-sm">
          {filterSummary(form, meta).join(' · ')}
        </span>
      </span>
      <span className="flex shrink-0 items-center gap-1.5 rounded-xl border border-line bg-surface-2 px-3 py-2 text-sm text-muted transition group-hover:text-ink">
        <SlidersHorizontal className="size-4" />
        <span className="hidden sm:inline">Filtros</span>
      </span>
    </button>
  )
}
