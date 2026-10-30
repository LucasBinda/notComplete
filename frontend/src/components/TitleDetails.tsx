import { Clock, ExternalLink, Star, Tv, X } from 'lucide-react'
import { formatCertification, formatOffer, formatRuntime, shortProviderName } from '../lib/format'
import { useModalDialog } from '../lib/useModalDialog'
import type { Recommendation } from '../types'

interface Props {
  title: Recommendation | null
  onClose: () => void
}

export function TitleDetails({ title, onClose }: Props) {
  const dialogRef = useModalDialog(title !== null)

  return (
    <dialog
      ref={dialogRef}
      onClose={onClose}
      onClick={(e) => e.target === dialogRef.current && onClose()}
      className="m-auto w-[min(56rem,calc(100%-2rem))] overflow-hidden rounded-2xl border border-line bg-surface p-0 text-ink backdrop:bg-black/70 backdrop:backdrop-blur-sm"
    >
      {title && (
        <div className="max-h-[85vh] overflow-y-auto">
          <div className="relative h-44 bg-surface-2 sm:h-64">
            {title.backdropUrl && <img src={title.backdropUrl} alt="" className="size-full object-cover opacity-60" />}
            <div className="absolute inset-0 bg-gradient-to-t from-surface via-surface/30 to-transparent" />
            <button
              type="button"
              onClick={onClose}
              aria-label="Fechar"
              className="absolute top-3 right-3 rounded-full bg-black/60 p-2 transition hover:bg-black/80"
            >
              <X className="size-5" />
            </button>
          </div>

          <div className="-mt-20 flex flex-col gap-6 p-5 sm:-mt-28 sm:flex-row sm:p-8">
            {title.posterUrl && (
              <img src={title.posterUrl} alt="" className="relative w-32 shrink-0 rounded-xl shadow-2xl sm:w-44" />
            )}
            <div className="relative min-w-0 sm:pt-20">
              <h2 className="text-2xl font-bold sm:text-3xl">{title.title}</h2>
              {title.originalTitle && title.originalTitle !== title.title && (
                <p className="text-sm text-muted">{title.originalTitle}</p>
              )}

              <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-ink/85">
                {title.year && <span>{title.year}</span>}
                {title.mediaType === 'movie' && title.runtime && (
                  <span className="flex items-center gap-1.5">
                    <Clock className="size-4 text-muted" /> {formatRuntime(title.runtime)}
                  </span>
                )}
                {title.mediaType === 'tv' && (
                  <span className="flex items-center gap-1.5">
                    <Tv className="size-4 text-muted" /> {title.seasons ? `${title.seasons} temporadas` : 'Série'}
                  </span>
                )}
                {title.voteAverage ? (
                  <span className="flex items-center gap-1.5">
                    <Star className="size-4 fill-star text-star" /> {title.voteAverage.toFixed(1)}
                  </span>
                ) : null}
                {title.certification && (
                  <span className="rounded-md border border-line px-2 py-0.5">{formatCertification(title.certification)}</span>
                )}
              </div>

              {title.genres && (
                <p className="mt-2 text-sm text-muted">{title.genres.map((g) => g.name).join(' · ')}</p>
              )}

              {title.reason && (
                <p className="mt-4 rounded-xl border border-brand/30 bg-brand/10 px-4 py-3 text-[15px] text-ink/90">
                  {title.reason}
                </p>
              )}

              {title.overview && <p className="mt-4 leading-relaxed text-ink/85">{title.overview}</p>}

              <h3 className="mt-6 mb-3 font-semibold">Onde assistir</h3>
              <ul className="space-y-2">
                {title.watch.providers.map((p) => (
                  <li key={p.id} className="flex items-center gap-3 rounded-xl border border-line bg-surface-2 p-2.5">
                    {p.logoUrl && <img src={p.logoUrl} alt="" className="size-10 rounded-lg" />}
                    <div className="min-w-0 flex-1">
                      <p className="font-medium">{shortProviderName(p.name)}</p>
                      <p className="text-xs text-muted">{formatOffer(p.offer)}</p>
                    </div>
                    <a
                      href={p.watchUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex items-center gap-1.5 rounded-lg bg-brand px-3 py-2 text-sm font-semibold text-page transition hover:bg-brand-strong"
                    >
                      Abrir <ExternalLink className="size-4" />
                    </a>
                  </li>
                ))}
              </ul>
              <p className="mt-2 text-xs text-faint">
                O botão abre a busca da plataforma. Disponibilidade via JustWatch, pode estar desatualizada.
              </p>
            </div>
          </div>
        </div>
      )}
    </dialog>
  )
}
