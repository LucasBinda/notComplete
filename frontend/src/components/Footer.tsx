export function Footer() {
  return (
    <footer className="border-t border-line/70">
      <div className="mx-auto max-w-7xl space-y-2 px-4 py-8 text-sm text-muted sm:px-6">
        <p>Sugestões baseadas em metadados públicos; a disponibilidade pode estar desatualizada.</p>
        {/* TODO: adicionar o logo oficial do TMDB (menos destacado que o nosso), conforme os termos da API */}
        <p>
          Dados de filmes e séries:{' '}
          <a href="https://www.themoviedb.org" target="_blank" rel="noopener noreferrer" className="text-brand-soft hover:underline">
            TMDB
          </a>
          . Dados de onde assistir:{' '}
          <a href="https://www.justwatch.com" target="_blank" rel="noopener noreferrer" className="text-brand-soft hover:underline">
            JustWatch
          </a>
          .
        </p>
        <p className="text-faint">This product uses the TMDB API but is not endorsed or certified by TMDB.</p>
      </div>
    </footer>
  )
}
