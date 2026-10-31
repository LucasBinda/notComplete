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

`npm run dev` usa um **backend falso** (`src/api/mockApi.ts`), útil quando os serviços não estão rodando:

- os títulos são **reais**, gerados do TMDB e salvos em `src/mocks/catalog.json`;
- os filtros (plataforma, formato, gênero, duração) funcionam;
- o texto livre **não** é interpretado e as justificativas são montadas por modelo.

## Com o backend de verdade

Suba os três serviços (ver `backend/README.md`) e rode:

```bash
npm run dev:backend   # o mesmo que VITE_USE_MOCKS=false
```

Em desenvolvimento ainda não há gateway: o Vite repassa `/api/recommend` para o recommendation-service
(`localhost:8082`) e o resto de `/api` para o catalog-service (`localhost:8081`). Ver `vite.config.ts`.

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
