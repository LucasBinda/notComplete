import { LoaderCircle } from 'lucide-react'
import { useEffect, useState } from 'react'
import { getMeta, recommend } from './api/client'
import { Footer } from './components/Footer'
import { Header } from './components/Header'
import { ResultsPanel } from './components/ResultsPanel'
import { SearchModal } from './components/SearchModal'
import { TitleDetails } from './components/TitleDetails'
import type { SearchForm } from './lib/searchForm'
import type { Criteria, MediaType, Meta, Recommendation, RecommendRequest, RecommendResponse } from './types'

const PROVIDERS_KEY = 'watchmatch.providers'
const DEFAULT_PROVIDERS = [8, 119] // Netflix e Prime Video
const PAGE_SIZE = 12

// try/catch: o localStorage pode estar bloqueado (modo anônimo)
function loadProviders(): number[] {
  try {
    const saved = JSON.parse(localStorage.getItem(PROVIDERS_KEY) ?? 'null')
    return Array.isArray(saved) ? saved : DEFAULT_PROVIDERS
  } catch {
    return DEFAULT_PROVIDERS
  }
}

function saveProviders(providers: number[]) {
  try {
    localStorage.setItem(PROVIDERS_KEY, JSON.stringify(providers))
  } catch {
  }
}

function toFilters(form: SearchForm): Criteria {
  const filters: Criteria = {}
  if (form.mediaType) filters.mediaType = form.mediaType as MediaType
  if (form.genre) filters.genres = [Number(form.genre)]
  if (form.runtimeMax) filters.runtimeMax = Number(form.runtimeMax)
  return filters
}

export default function App() {
  const [meta, setMeta] = useState<Meta | null>(null)
  const [form, setForm] = useState<SearchForm>({
    query: '',
    genre: '',
    runtimeMax: '',
    mediaType: '',
    providers: loadProviders(),
  })
  const [result, setResult] = useState<RecommendResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  // "Carregar mais" repete este pedido com a próxima página
  const [lastRequest, setLastRequest] = useState<RecommendRequest | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [selected, setSelected] = useState<Recommendation | null>(null)
  const [searchOpen, setSearchOpen] = useState(false)

  useEffect(() => {
    getMeta()
      .then(setMeta)
      .catch(() => setError('Não foi possível carregar gêneros e plataformas.'))
  }, [])

  useEffect(() => saveProviders(form.providers), [form.providers])

  async function search(current: SearchForm) {
    setLoading(true)
    setError(null)
    const request: RecommendRequest = {
      query: current.query.trim() || undefined,
      filters: toFilters(current),
      providers: current.providers,
      limit: PAGE_SIZE,
    }
    try {
      setResult(await recommend(request))
      setLastRequest(request)
    } catch (e) {
      setResult(null)
      setError(e instanceof Error ? e.message : 'Algo deu errado. Tente de novo.')
    } finally {
      setLoading(false)
    }
  }

  async function loadMore() {
    if (!result || !lastRequest) return
    setLoadingMore(true)
    setError(null)
    try {
      const next = await recommend({ ...lastRequest, page: result.page + 1 })
      // A IA pode repetir um título entre páginas; mantém só a primeira aparição
      const seen = new Set(result.items.map((t) => `${t.mediaType}-${t.id}`))
      const fresh = next.items.filter((t) => !seen.has(`${t.mediaType}-${t.id}`))
      setResult({ ...result, items: [...result.items, ...fresh], page: next.page, hasMore: next.hasMore })
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Não foi possível carregar mais títulos.')
    } finally {
      setLoadingMore(false)
    }
  }

  function submitSearch() {
    setSearchOpen(false)
    search(form)
  }

  function searchExample(query: string) {
    const next = { ...form, query }
    setForm(next)
    search(next)
  }

  return (
    <div className="min-h-dvh">
      <Header />

      <main id="descobrir" className="mx-auto max-w-7xl px-4 py-10 sm:px-6 sm:py-14">
        <div className="space-y-8">
          <div>
            <h1 className="max-w-3xl text-3xl leading-tight font-bold tracking-tight sm:text-4xl lg:text-5xl">
              Filmes e séries para hoje, nas plataformas que você já assina
            </h1>
            <p className="mt-3 max-w-2xl text-lg text-muted">
              Escreva do seu jeito, como “comédia curta para ver em casal”, e veja onde assistir.
            </p>
          </div>

          {meta ? (
            <div className="space-y-6">
              <SearchModal
                open={searchOpen}
                meta={meta}
                form={form}
                loading={loading}
                onOpen={() => setSearchOpen(true)}
                onClose={() => setSearchOpen(false)}
                onChange={setForm}
                onSubmit={submitSearch}
              />
              <ResultsPanel
                meta={meta}
                result={result}
                loading={loading}
                loadingMore={loadingMore}
                error={error}
                onLoadMore={loadMore}
                onExample={searchExample}
                onOpen={setSelected}
              />
            </div>
          ) : error ? (
            <p className="text-star">{error}</p>
          ) : (
            <p className="flex items-center gap-2 text-muted">
              <LoaderCircle className="size-5 animate-spin" /> Carregando…
            </p>
          )}
        </div>
      </main>

      <Footer />
      <TitleDetails title={selected} onClose={() => setSelected(null)} />
    </div>
  )
}
