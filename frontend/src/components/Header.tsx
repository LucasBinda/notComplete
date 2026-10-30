import { Play } from 'lucide-react'

const links = [
  { label: 'Descobrir', href: '#descobrir', active: true },
  { label: 'Minha lista', href: '#minha-lista', soon: true },
]

export function Header() {
  return (
    <header className="sticky top-0 z-30 border-b border-line/70 bg-page/80 backdrop-blur">
      <div className="mx-auto flex max-w-7xl flex-wrap items-center gap-x-10 gap-y-2 px-4 py-3 sm:px-6">
        <a href="#descobrir" className="flex items-center gap-2" aria-label="AjudadorDePublico, início">
          <Play className="size-7 fill-brand text-brand" />
          <span className="text-xl font-extrabold tracking-tight">
            AjudadorDe<span className="text-brand-soft">Publico</span>
          </span>
        </a>

        <nav className="-mx-1 flex gap-1 overflow-x-auto text-sm [scrollbar-width:none] sm:text-[15px]">
          {links.map((link) => (
            <a
              key={link.label}
              href={link.href}
              aria-current={link.active ? 'page' : undefined}
              aria-disabled={link.soon || undefined}
              title={link.soon ? 'Em breve' : undefined}
              className={`relative whitespace-nowrap px-3 py-2 transition-colors ${
                link.active ? 'text-ink' : 'text-muted hover:text-ink'
              } ${link.soon ? 'pointer-events-none opacity-60' : ''}`}
            >
              {link.label}
              {link.soon && (
                <span className="ml-1.5 hidden rounded bg-surface-2 px-1.5 py-0.5 text-[10px] uppercase tracking-wide sm:inline">
                  em breve
                </span>
              )}
              {link.active && <span className="absolute inset-x-3 -bottom-[13px] h-0.5 rounded bg-brand" />}
            </a>
          ))}
        </nav>
      </div>
    </header>
  )
}
