import { ChevronDown } from 'lucide-react'

interface Option {
  value: string
  label: string
}

interface Props {
  id: string
  label: string
  value: string
  options: Option[]
  onChange: (value: string) => void
}

export function SelectField({ id, label, value, options, onChange }: Props) {
  return (
    <div>
      <label htmlFor={id} className="mb-2 block text-sm text-ink/90">
        {label}
      </label>
      <div className="relative">
        <select
          id={id}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className="w-full appearance-none rounded-xl border border-line bg-surface-2 px-4 py-2.5 pr-10 text-[15px] outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/30"
        >
          {options.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
        <ChevronDown className="pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2 text-muted" />
      </div>
    </div>
  )
}
