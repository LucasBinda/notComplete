# Arquitetura e plano técnico — WatchMatch

Documento vivo; decisões tomadas em 27/10/2026 para o C2 e refinadas até o C3.

## 1. Visão geral

O WatchMatch é um **site independente** (não é extensão da Netflix/Prime). O backend segue uma arquitetura de **microsserviços**: cada parte é um programa separado, com deploy próprio, que se comunica com os outros via HTTP/JSON.

```
frontend ─▶ gateway ─▶ recommendation-service ─┬─▶ llm-service ─────▶ Groq
                   │                            └─▶ catalog-service ─▶ TMDB
                   ├─▶ catalog-service (detalhes de título)
                   └─▶ feedback-service ─▶ Postgres (C3)
```

O `llm-service` **não** tem rota pública: só é acessível pela rede interna.

## 2. Serviços

| Serviço | Responsabilidade | Dono de | Entrega |
| ------- | ---------------- | ------- | ------- |
| **gateway** | Porta única do frontend: roteamento, CORS, rate limit por IP, limite de tamanho do corpo, geração do `X-Request-Id` | nada | C2 |
| **recommendation-service** | Orquestrador do fluxo de recomendação (seção 4) | nada (stateless) | C2 |
| **llm-service** | Integração com o Groq: `POST /intent` e `POST /rerank`; prompts versionados; validação do JSON do LLM | chave do Groq | C2 |
| **catalog-service** | Integração com o TMDB: discover, detalhes, onde assistir, gêneros e plataformas do BR; cache | chave do TMDB | C2 |
| **feedback-service** | "Gostei"/"Não gostei"/"Vou assistir" e métricas de aceitação | banco Postgres próprio | C3 |

Princípios:

- Cada serviço é **dono dos seus segredos e dados**; nenhum serviço acessa o banco ou a API externa de outro.
- Comunicação **síncrona REST/JSON**. Fila (RabbitMQ/Kafka) só se fizer sentido no C3 (ex.: eventos de feedback).

## 3. Linguagens e liberdade de escolha

**Padrão:** Java 21 + Spring Boot 3 (testes com JUnit 5, Mockito e WireMock).
**Gateway:** Nginx (configuração, sem código). Pode virar Spring Cloud Gateway se precisar de lógica própria.

Cada desenvolvedor **pode escolher outra linguagem** para o serviço que implementa, desde que cumpra os combinados abaixo. O serviço é uma caixa-preta: vale o que ele promete por fora.

### 3.1 Contrato primeiro

- Antes do código, o serviço ganha `backend/contracts/<servico>.yaml` (**OpenAPI 3**) com rotas, entradas e saídas.
- O contrato é a fonte da verdade; qualquer implementação que o cumpra é válida.
- **Prism** gera um mock do serviço a partir do contrato, permitindo desenvolver os outros serviços e o frontend antes da implementação real.
- Contratos atuais: `common.yaml` (esquemas compartilhados), `catalog-service.yaml`, `llm-service.yaml` e `recommendation-service.yaml`. Validação: `npx @redocly/cli lint` dentro de `backend/contracts/`.
- O que cada API externa oferece e as decisões de modelo/fonte estão em [APIS.md](./APIS.md).

### 3.2 Convenções obrigatórias (todos os serviços)

| Item | Regra |
| ---- | ----- |
| Entrega | `Dockerfile` na pasta do serviço; build com a ferramenta da linguagem dentro do container |
| Configuração | Somente por variáveis de ambiente (`PORT`, `GROQ_API_KEY`, `CATALOG_URL`…), nunca fixa no código |
| Saúde | `GET /health` → `200` quando o serviço está de pé |
| Rastreamento | Recebe e repassa o cabeçalho `X-Request-Id` em toda chamada |
| Erros | `application/problem+json`: `{ "status", "title", "detail" }` |
| Logs | JSON na saída padrão, incluindo `requestId` |
| Timeouts | Toda chamada HTTP a outro serviço ou API externa tem timeout definido |
| README | Linguagem escolhida, motivo, como rodar e testar |

### 3.3 Testes de contrato

**Schemathesis** lê o OpenAPI e dispara requisições contra o serviço rodando, verificando se as respostas batem com o contrato — o mesmo teste vale para qualquer linguagem. Roda no GitHub Actions para cada serviço.

## 4. Fluxo de uma recomendação

```
frontend ──POST /api/recommend {texto, filtros}──▶ gateway ──▶ recommendation-service
  1. Valida e sanitiza o texto (máx. ~500 caracteres)
  2. llm-service /intent: texto → JSON {tipo, gêneros, excluir_gêneros, duração_máx,
     anos, idioma, clima/palavras-chave, título_referência}
  3. Mescla com os filtros explícitos (filtro da UI sempre vence o LLM)
  4. catalog-service /discover (BR + plataformas) → ~30–40 candidatos reais
     (+ recomendações por similaridade quando há "parecido com X")
  5. llm-service /rerank: recebe SÓ os candidatos (id, título, sinopse) →
     escolhe os ~8 melhores + justificativa curta de cada
  6. Grounding: descarta qualquer id que não esteja entre os candidatos
  7. Resposta: critérios interpretados + cards (pôster, ano, duração, onde assistir, motivo)
```

O passo 6 é a mitigação de alucinação e prompt injection descrita em [IA_RESPONSAVEL.md](./IA_RESPONSAVEL.md): o LLM nunca cria títulos, só escolhe entre itens reais do catálogo.

### Resiliência

- **Timeout + circuit breaker** (Resilience4j) no recommendation-service.
- Se o llm-service falhar, a resposta sai com os resultados do catálogo **sem justificativas** (degradação controlada).
- Demonstração prevista no vídeo: derrubar o container do llm-service e mostrar o app continuando a funcionar.

## 5. Gateway

Rotas públicas (tudo o que não está listado é bloqueado):

| Rota pública | Destino |
| ------------ | ------- |
| `POST /api/recommend` | recommendation-service |
| `GET /api/titles/:type/:id` | catalog-service |
| `GET /api/meta` | catalog-service (gêneros e plataformas do BR) |
| `POST /api/feedback` | feedback-service (C3) |
| `GET /health` | gateway |

Responsabilidades centralizadas no gateway: CORS, `limit_req` por IP (protege o free tier do Groq), limite de tamanho do corpo e `X-Request-Id` (`$request_id` do Nginx).

## 6. Estrutura do repositório

```
backend/
  gateway/                   nginx.conf · Dockerfile
  recommendation-service/
  llm-service/               (inclui prompts/ versionados)
  catalog-service/
  feedback-service/          (C3)
  contracts/                 OpenAPI de cada serviço
  eval/                      casos e script de eval (chama o gateway via HTTP)
  docker-compose.yml         sobe tudo com `docker compose up`
  .env.example               GROQ_API_KEY, TMDB_API_KEY
frontend/
docs/
  ARQUITETURA.md · EVAL.md
  prompts/                   registro do uso de IA no desenvolvimento (exigido pelo edital)
trash/                       lixeira local, ignorada pelo git
```

## 7. Frontend

Previsto: Vite + React + TypeScript + Tailwind (a confirmar). O frontend conhece **apenas o gateway**.

### Telas

1. **Home / busca** — campo grande ("O que você quer assistir hoje?"), chips de exemplo, seleção das plataformas assinadas (salva no `localStorage`, sem cadastro) e painel de filtros recolhível (filme/série, gênero, duração, ano, classificação, idioma).
2. **Resultados** — critérios interpretados em chips editáveis ("Entendi: comédia · até 2h · Netflix") e grade de cards com o motivo da IA, logos das plataformas e "Gostei"/"Não gostei"/"Vou assistir".
3. **Detalhe** (gaveta/modal) — sinopse, elenco, classificação, onde assistir, "mais parecidos".
4. **Sobre / Privacidade** — limitações, fontes e **atribuição obrigatória ao TMDB e ao JustWatch**.
5. *(C3, opcional)* "Minha lista" no `localStorage` e página interna com resultados do eval.

## 8. Testes e eval

| Nível | Ferramenta | O que cobre |
| ----- | ---------- | ----------- |
| Unitário | JUnit 5 + Mockito (ou equivalente da linguagem) | sanitização, mescla de filtros, parse do LLM, grounding |
| Integração | WireMock (Groq/TMDB simulados) | cada serviço isolado contra APIs externas falsas |
| Contrato | Schemathesis | cada serviço cumpre seu OpenAPI |
| Ponta a ponta | Docker Compose + chamadas ao gateway | fluxo completo, incluindo fallback sem LLM |

**Eval do componente de IA** (`backend/eval/`): 15–30 casos em `cases.json` com a frase e o esperado (plataforma, gênero, duração máxima). Roda contra o gateway, independente da linguagem dos serviços.

| Métrica | Meta |
| ------- | ---- |
| JSON válido na saída do LLM | ~100% |
| Recomendações disponíveis na plataforma pedida | ~100% |
| Critérios respeitados (gênero, duração, ano) | alto |
| Títulos fora do catálogo de candidatos | 0 |
| Latência por requisição | registrar (p50/p95) |

## 9. Plano até o C2 (30/10/2026)

| Dia | Backend | Frontend |
| --- | ------- | -------- |
| **28/10** | Contratos OpenAPI; `docker-compose.yml`; gateway Nginx; catalog-service | Projeto, Home, seleção de plataformas, cards usando mock do Prism |
| **29/10** | llm-service (intent + rerank); recommendation-service com grounding e fallback; testes principais | Integração com o gateway, chips de critérios, gaveta de detalhe |
| **30/10** | 5–10 casos de eval; ajustes; gravação do vídeo | |

**Roteiro do vídeo:** problema → arquitetura (diagrama) → 3 buscas ao vivo → critérios interpretados → derrubar o llm-service e mostrar o fallback → eval rodando.

**Fica para o C3:** feedback-service + Postgres, deploy na nuvem (avaliar free tier com vários serviços vs. VM rodando o Compose), Playwright, Watchmode, PWA, possível reescrita de um serviço em outra linguagem (ex.: catalog-service em C++).

## 10. Pendências

- Adicionar o professor como colaborador no GitHub (exigência do C2).
- Criar as chaves gratuitas do Groq e do TMDB e preencher o `.env` (nunca versionar).
