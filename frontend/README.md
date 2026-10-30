# Frontend — WatchMatch

Interface web em **React + TypeScript**, com **Vite** e **Tailwind CSS**. Conversa só com o gateway (`/api/...`).

## Como rodar

```bash
npm install
npm run dev        # http://localhost:5173
npm run build      # checa os tipos e gera dist/
npm run lint
```

## Modo demonstração (dados de exemplo)

Enquanto os microsserviços não estão prontos, o site usa um **backend falso** (`src/api/mockApi.ts`):

- os títulos são **reais**, gerados do TMDB e salvos em `src/mocks/catalog.json`;
- os filtros (plataforma, formato, gênero, duração) funcionam;
- o texto livre **não** é interpretado e as justificativas são montadas por modelo — a IA entra com o llm-service.

Para usar o backend de verdade (gateway rodando em `localhost:8080`), crie `frontend/.env.local`:

```
VITE_USE_MOCKS=false
```

Em desenvolvimento, o Vite repassa `/api` para `http://localhost:8080` (ver `vite.config.ts`).

## Estrutura

```
src/
  App.tsx                 estado da página e busca
  types.ts                tipos espelhando os contratos em backend/contracts/
  api/client.ts           acesso ao gateway (ou ao mock)
  api/mockApi.ts          backend falso para desenvolvimento
  mocks/catalog.json      títulos e plataformas reais do TMDB
  lib/format.ts           formatação (duração, classificação, chips "Entendi")
  lib/searchForm.ts       estado do formulário de busca e opções dos filtros
  lib/useModalDialog.ts   abre/fecha um <dialog> nativo a partir do estado
  components/
    Header · SearchBar · SearchModal · SearchFields · SelectField · PlatformPicker
    ResultsPanel · CriteriaChips · TitleCard · TitleDetails
    Footer
```

## Pendências

- Logo oficial do TMDB no rodapé (exigência dos termos da API).
- Aba "Minha lista" (C3, salva no navegador).
