import { ArrowRight, Clock, Clapperboard, Star, Tv } from 'lucide-react'
import { formatCertification, formatRuntime } from '../lib/format'
import type { Recommendation } from '../types'

interface Props {
  title: Recommendation
  onOpen: () => void
}

export function TitleCard({ title, onOpen }: Props) {
  const genres = (title.genres ?? []).slice(0, 2)

  return (
    <article className="group flex flex-col overflow-hidden rounded-2xl border border-line bg-surface-2/60 transition hover:-translate-y-0.5 hover:border-brand/60">
      <button type="button" onClick={onOpen} className="relative block aspect-[2/3] overflow-hidden bg-surface-2" aria-label={`Ver detalhes de ${title.title}`}>
        {title.posterUrl ? (
          <img
            src={title.posterUrl}
            alt=""
            loading="lazy"
            className="size-full object-cover transition duration-300 group-hover:scale-[1.03]"
          />
        ) : (
          <div className="grid size-full place-items-center text-faint">
            <Clapperboard className="size-12" />
          </div>
        )}

        {title.certification && (
          <span className="absolute top-3 left-3 rounded-md bg-black/70 px-2 py-0.5 text-xs font-semibold backdrop-blur">
            {formatCertification(title.certification)}
          </span>
        )}

        <div className="absolute right-3 bottom-3 flex gap-1.5">
          {title.watch.providers.map((p) =>
            p.logoUrl ? (
              <img key={p.id} src={p.logoUrl} alt={p.name} title={p.name} className="size-8 rounded-lg shadow-lg ring-1 ring-black/40" />
            ) : null,
          )}
        </div>
      </button>

      <div className="flex flex-1 flex-col p-4">
        <h3 className="text-lg leading-snug font-semibold">
          {title.title}
          {title.year && <span className="ml-1.5 text-sm font-normal text-muted">({title.year})</span>}
        </h3>

        {genres.length > 0 && (
          <p className="mt-1 text-sm text-muted">{genres.map((g) => g.name).join(' · ')}</p>
        )}

        <div className="mt-3 flex items-center gap-4 text-sm">
          {title.mediaType === 'movie' && title.runtime ? (
            <span className="flex items-center gap-1.5">
              <Clock className="size-4 text-muted" />
              {formatRuntime(title.runtime)}
            </span>
          ) : title.mediaType === 'tv' ? (
            <span className="flex items-center gap-1.5">
              <Tv className="size-4 text-muted" />
              {title.seasons ? `${title.seasons} temporada${title.seasons > 1 ? 's' : ''}` : 'Série'}
            </span>
          ) : null}
          {title.voteAverage ? (
            <span className="flex items-center gap-1.5">
              <Star className="size-4 fill-star text-star" />
              {title.voteAverage.toFixed(1)}
            </span>
          ) : null}
        </div>

        {title.reason && <p className="mt-3 text-[15px] leading-relaxed text-ink/85">{title.reason}</p>}

        <button
          type="button"
          onClick={onOpen}
          className="mt-auto flex items-center gap-1.5 pt-4 text-sm font-semibold text-brand-soft transition hover:gap-2.5 hover:text-ink"
        >
          Ver detalhes <ArrowRight className="size-4" />
        </button>
      </div>
    </article>
  )
}
