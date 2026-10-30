import { Sparkles } from 'lucide-react'
import type { CriteriaChip } from '../lib/format'

interface Props {
  chips: CriteriaChip[]
  summary?: string | null
}

export function CriteriaChips({ chips, summary }: Props) {
  if (chips.length === 0 && !summary) return null

  return (
    <div className="flex flex-wrap items-center gap-2 text-sm">
      <span className="text-muted">Entendi:</span>
      {summary && <span className="font-medium text-ink">{summary}</span>}
      {chips.map((chip) => (
        <span
          key={chip.label}
          title={chip.source === 'ai' ? 'Interpretado pela IA' : 'Filtro escolhido por você'}
          className={`flex items-center gap-1 rounded-full border px-3 py-1 ${
            chip.source === 'ai'
              ? 'border-brand/50 bg-brand/15 text-brand-soft'
              : 'border-line bg-surface-2 text-ink/85'
          }`}
        >
          {chip.source === 'ai' && <Sparkles className="size-3.5" />}
          {chip.label}
        </span>
      ))}
    </div>
  )
}
