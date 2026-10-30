import { RotateCcw, SlidersHorizontal, X } from 'lucide-react'
import type { FormEvent } from 'react'
import { SearchBar } from './SearchBar'
import { SearchFields, SearchSubmit } from './SearchFields'
import { clearSearch, type SearchForm } from '../lib/searchForm'
import { useModalDialog } from '../lib/useModalDialog'
import type { Meta } from '../types'

interface Props {
  open: boolean
  meta: Meta
  form: SearchForm
  loading: boolean
  onOpen: () => void
  onClose: () => void
  onChange: (form: SearchForm) => void
  onSubmit: () => void
}

export function SearchModal({ open, meta, form, loading, onOpen, onClose, onChange, onSubmit }: Props) {
  const dialogRef = useModalDialog(open)

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    onSubmit()
  }

  return (
    <>
      <SearchBar meta={meta} form={form} expanded={open} onOpen={onOpen} />
      <dialog
        ref={dialogRef}
        onClose={onClose}
        onClick={(e) => e.target === dialogRef.current && onClose()}
        className="m-auto w-[min(48rem,calc(100%-2rem))] max-w-none overflow-hidden rounded-2xl border border-line bg-surface p-0 text-ink backdrop:bg-black/70 backdrop:backdrop-blur-sm open:animate-pop-in max-sm:h-dvh max-sm:max-h-none max-sm:w-full max-sm:rounded-none max-sm:border-0"
      >
        {open && (
          <form onSubmit={handleSubmit} className="flex max-h-[85vh] flex-col max-sm:h-full max-sm:max-h-none">
            <div className="flex items-center justify-between gap-3 border-b border-line px-5 py-4 sm:px-6">
              <h2 className="flex items-center gap-2.5 text-lg font-semibold">
                <SlidersHorizontal className="size-5 text-brand-soft" />
                Personalize sua busca
              </h2>
              <button
                type="button"
                onClick={onClose}
                aria-label="Fechar"
                className="rounded-lg p-1.5 text-muted transition hover:bg-surface-2 hover:text-ink"
              >
                <X className="size-5" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto px-5 py-5 sm:px-6">
              <SearchFields meta={meta} form={form} onChange={onChange} wide autoFocus />
            </div>

            <div className="flex flex-col-reverse gap-3 border-t border-line px-5 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
              <button
                type="button"
                onClick={() => onChange(clearSearch(form))}
                className="flex items-center justify-center gap-2 rounded-xl px-3 py-2 text-sm text-muted transition hover:text-ink"
              >
                <RotateCcw className="size-4" /> Limpar filtros
              </button>
              <SearchSubmit loading={loading} disabled={form.providers.length === 0} />
            </div>
          </form>
        )}
      </dialog>
    </>
  )
}
