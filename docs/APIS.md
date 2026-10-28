# APIs externas — verificação (28/10/2026)

As três APIs citadas no relatório estão no ar e exigem chave. Teste sem chave:

| API | Endpoint testado | Resposta |
| --- | ---------------- | -------- |
| TMDB | `GET https://api.themoviedb.org/3/configuration` | `401 Invalid API key` |
| Groq | `GET https://api.groq.com/openai/v1/models` | `401 Invalid API Key` |
| Watchmode | `GET https://api.watchmode.com/v1/sources/` | `401 Please enter a valid API key` |

**Ainda falta:** criar as chaves (todas gratuitas) e repetir o teste com elas, principalmente para confirmar a cobertura de plataformas no **BR**.

---

## TMDB — catálogo e "onde assistir" (usar)

- **Custo:** gratuita para uso não comercial.
- **Autenticação:** `Authorization: Bearer <token de leitura>` ou `?api_key=`.
- **Atribuição obrigatória:**
  - logo do TMDB (menos destacado que o nosso) na tela Sobre;
  - aviso: *"This product uses the TMDB API but is not endorsed or certified by TMDB."*;
  - dados de "onde assistir" creditados à **JustWatch**.
- **Imagens:** `https://image.tmdb.org/t/p/<tamanho><poster_path>` (ex. `w500`).

### Endpoints que vamos usar

| Endpoint | Uso | Serviço |
| -------- | --- | ------- |
| `GET /discover/movie` e `/discover/tv` | Busca de candidatos por critérios | catalog `/discover` |
| `GET /search/multi` (ou `/search/movie`, `/search/tv`) | Resolver "parecido com X" | catalog `/search` |
| `GET /movie/{id}` e `/tv/{id}` (com `append_to_response=watch/providers,credits,release_dates`) | Detalhes + onde assistir + elenco + classificação em uma chamada | catalog `/titles/...` |
| `GET /movie/{id}/recommendations` | Títulos parecidos | catalog `/titles/.../similar` |
| `GET /genre/movie/list` e `/genre/tv/list` | Lista de gêneros | catalog `/meta` |
| `GET /watch/providers/movie?watch_region=BR` | Plataformas disponíveis no BR | catalog `/meta` |
| `GET /search/keyword` | Converter palavra-chave em id | catalog `/discover` |

### Filtros do `discover` que cobrem nossos critérios

| Nosso critério | Parâmetro TMDB |
| -------------- | -------------- |
| plataformas | `watch_region=BR` + `with_watch_providers=8\|119` (`\|` = OU) |
| só incluído na assinatura | `with_watch_monetization_types=flatrate\|free\|ads` |
| gêneros / excluir | `with_genres` / `without_genres` |
| duração | `with_runtime.gte` / `with_runtime.lte` |
| ano | `primary_release_date.gte/.lte` (filme) · `first_air_date.gte/.lte` (série) |
| idioma original | `with_original_language` |
| classificação | `certification_country=BR` + `certification.lte` |
| palavras-chave | `with_keywords` (ids) |
| idioma da resposta | `language=pt-BR` |

**Retorno do discover (por item):** `id`, `title`, `original_title`, `overview`, `release_date`, `genre_ids`, `poster_path`, `backdrop_path`, `vote_average`, `vote_count`, `popularity`, `original_language`. **Não traz duração nem plataformas**, por isso o catalog-service tem `/titles/lookup` para enriquecer só os itens finais.

**Retorno de watch providers:** objeto por país (`results.BR`) com `link` (página do TMDB) e listas `flatrate` (assinatura), `rent`, `buy`, `ads`. Cada item: `provider_id`, `provider_name`, `logo_path`, `display_priority`.

### "Onde assistir" no nosso site

O que o TMDB permite fazer **dentro do WatchMatch**, sem o usuário sair do site:

- **Filtro de plataformas do BR:** a lista de plataformas com catálogo no Brasil (nome + logo) vem de `/watch/providers/movie?watch_region=BR` e é exposta pelo catalog-service em `/meta`.
- **Onde assistir em cada card:** logos das plataformas em que o título está no BR, com o tipo de oferta (assinatura, aluguel, compra).

A única coisa que o TMDB **não** fornece é o *deep link* (o endereço do título dentro da plataforma, ex. `netflix.com/title/70305903`).

**Solução no C2:** o botão "Abrir na plataforma" (`watchUrl` no contrato) leva à **busca da própria plataforma** com o nome do título. O catalog-service mantém uma tabela `provider_id → modelo de URL`; plataforma fora da tabela usa o `link` do TMDB.

| Plataforma | Modelo de URL de busca (testar antes de fixar) |
| ---------- | ---------------------------------------------- |
| Netflix | `https://www.netflix.com/search?q={titulo}` |
| Amazon Prime Video | `https://www.primevideo.com/search?phrase={titulo}` |
| Outras (Disney+, Max, Globoplay…) | a levantar; até lá, `link` do TMDB |

Os ids das plataformas no BR devem ser conferidos na resposta real de `/watch/providers/movie?watch_region=BR` (ex. Netflix = 8, Prime Video = 119).

**C3 (extra):** deep link real via Watchmode, consultado só quando o usuário clica (com cache).

Assistir ao vídeo **dentro** do WatchMatch não é possível com nenhuma API (conteúdo protegido) e está fora do escopo.

---

## Groq — LLM (usar)

- **Autenticação:** `Authorization: Bearer <GROQ_API_KEY>`. A API é compatível com a da OpenAI (`/openai/v1/chat/completions`).
- **Modelos de produção (texto):** `openai/gpt-oss-20b`, `openai/gpt-oss-120b`, `llama-3.3-70b-versatile`, `llama-3.1-8b-instant` (todos com contexto de 131k).
- **Saída estruturada:**
  - `response_format: { type: "json_schema", json_schema: { name, strict: true, schema } }`;
  - **modo estrito** (JSON garantido no schema) só em `openai/gpt-oss-20b` e `openai/gpt-oss-120b`;
  - no estrito, todos os campos precisam estar em `required` (os opcionais viram `["tipo","null"]`) e todo objeto precisa ter `additionalProperties: false`;
  - sem streaming e sem tool use junto;
  - os outros modelos só têm `json_object`: JSON válido, mas sem garantia de schema.
- **Limites (tabela base do plano Developer)** para os gpt-oss: **30 req/min, 1.000 req/dia, 8k tokens/min, 200k tokens/dia**. O free tier pode ser menor; conferir em *Settings → Limits* depois de criar a conta.

### Decisão

| Rota | Modelo | Motivo |
| ---- | ------ | ------ |
| `llm-service /intent` | `openai/gpt-oss-20b` | Rápido; modo estrito garante o JSON dos critérios |
| `llm-service /rerank` | `openai/gpt-oss-120b` | Precisa "entender" sinopses; modo estrito |

Cada recomendação = **2 chamadas** ao Groq, o que dá ~500 recomendações/dia no limite de 1.000 req/dia. É suficiente para o C2/C3, mas reforça a necessidade do rate limit no gateway. Os modelos ficam em variável de ambiente para trocar sem mexer no código.

---

## Watchmode — "onde assistir" (não usar no C2)

- **Plano gratuito (site oficial):** 2.500 requisições/mês, uso não comercial, **até 3 países**, atribuição obrigatória. Pode cachear por até 30 dias.
- Consultas com dados "anexados" custam mais de 1 crédito.
- **Vantagem sobre o TMDB:** deep links direto para o título na plataforma.
- **Decisão:** o TMDB já cobre "onde assistir" no BR sem limite mensal apertado, e o botão "Abrir na plataforma" usa a busca da plataforma no C2. O Watchmode fica como **extra no C3** para trocar a busca pelo deep link, se a cobertura do BR for confirmada.

---

## Fontes

- [TMDB — Discover Movie](https://developer.themoviedb.org/reference/discover-movie)
- [TMDB — Watch Providers](https://developer.themoviedb.org/reference/movie-watch-providers)
- [TMDB — FAQ (termos e atribuição)](https://developer.themoviedb.org/docs/faq)
- [Groq — Models](https://console.groq.com/docs/models)
- [Groq — Structured Outputs](https://console.groq.com/docs/structured-outputs)
- [Groq — Rate Limits](https://console.groq.com/docs/rate-limits)
- [Watchmode API](https://api.watchmode.com/)
