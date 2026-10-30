import { Check } from 'lucide-react'
import { shortProviderName } from '../lib/format'
import type { Provider } from '../types'

interface Props {
  providers: Provider[]
  selected: number[]
  onToggle: (id: number) => void
  gridClassName?: string
}

export function PlatformPicker({ providers, selected, onToggle, gridClassName = 'grid-cols-2' }: Props) {
  return (
    <fieldset>
      <legend className="mb-2 text-sm text-ink/90">
        Plataformas que você assina
      </legend>
      <div className={`grid gap-2 ${gridClassName}`}>
        {providers.map((p) => {
          const isOn = selected.includes(p.id)
          return (
            <button
              key={p.id}
              type="button"
              aria-pressed={isOn}
              onClick={() => onToggle(p.id)}
              className={`relative flex items-center gap-2 rounded-xl border px-2.5 py-2 text-left text-xs font-medium transition ${
                isOn
                  ? 'border-brand bg-brand/15 text-ink'
                  : 'border-line bg-surface-2 text-muted hover:border-faint hover:text-ink'
              }`}
            >
              {p.logoUrl && <img src={p.logoUrl} alt="" className="size-7 shrink-0 rounded-md" />}
              <span className="line-clamp-2">{shortProviderName(p.name)}</span>
              {isOn && (
                <Check className="absolute -top-1.5 -right-1.5 size-4 rounded-full bg-brand p-0.5 text-page" />
              )}
            </button>
          )
        })}
      </div>
      {selected.length === 0 && (
        <p className="mt-2 text-xs text-star">Escolha pelo menos uma plataforma.</p>
      )}
    </fieldset>
  )
}
