// Mesmos formatos de backend/contracts/. Mudou o contrato, mude aqui.

export type MediaType = 'movie' | 'tv'

export type Certification = 'L' | '10' | '12' | '14' | '16' | '18'

export interface Genre {
  id: number
  name: string
}

export interface Provider {
  id: number
  name: string
  logoUrl?: string | null
}

export interface WatchProvider extends Provider {
  offer: 'flatrate' | 'free' | 'ads' | 'rent' | 'buy'
  watchUrl: string
}

export interface Criteria {
  mediaType?: MediaType
  genres?: number[]
  excludeGenres?: number[]
  runtimeMin?: number
  runtimeMax?: number
  yearFrom?: number
  yearTo?: number
  originalLanguage?: string
  certificationMax?: Certification
  keywords?: string[]
}

export interface Meta {
  genres: { movie: Genre[]; tv: Genre[] }
  providers: Provider[]
}

export interface RecommendRequest {
  query?: string
  filters?: Criteria
  providers: number[]
  limit?: number
  page?: number
}

export interface AppliedCriteria extends Criteria {
  providers: number[]
  referenceTitle?: string | null
  summary?: string | null
  sources?: Record<string, 'filter' | 'ai'>
}

export interface Recommendation {
  id: number
  mediaType: MediaType
  title: string
  originalTitle?: string
  year?: number | null
  overview?: string
  posterUrl?: string | null
  backdropUrl?: string | null
  genres?: Genre[]
  voteAverage?: number
  runtime?: number | null
  seasons?: number | null
  certification?: Certification | null
  watch: { link?: string | null; providers: WatchProvider[] }
  reason?: string | null
}

export interface RecommendResponse {
  requestId?: string
  criteria: AppliedCriteria
  items: Recommendation[]
  page: number
  hasMore: boolean
  degraded: boolean
  notices: string[]
}
