export interface SearchForm {
  query: string
  genre: string // id do gênero ou '' para qualquer
  runtimeMax: string // minutos ou ''
  mediaType: string // 'movie' | 'tv' | ''
  providers: number[]
}

export const RUNTIME_OPTIONS = [
  { value: '', label: 'Qualquer duração' },
  { value: '90', label: 'Até 1h30' },
  { value: '120', label: 'Até 2 horas' },
  { value: '150', label: 'Até 2h30' },
]

export const MEDIA_OPTIONS = [
  { value: '', label: 'Filme e série' },
  { value: 'movie', label: 'Só filmes' },
  { value: 'tv', label: 'Só séries' },
]

// Mantém as plataformas: elas são do usuário, não da busca
export function clearSearch(form: SearchForm): SearchForm {
  return { ...form, query: '', genre: '', runtimeMax: '', mediaType: '' }
}
