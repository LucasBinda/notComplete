# Backend — WatchMatch

Backend em **microsserviços**, orquestrados com Docker Compose. Arquitetura completa em [docs/ARQUITETURA.md](../docs/ARQUITETURA.md).

## Serviços

| Serviço | Responsabilidade |
| ------- | ---------------- |
| `gateway/` | Porta única do frontend (Nginx): roteamento, CORS, rate limit |
| `recommendation-service/` | Orquestra o fluxo de recomendação |
| `llm-service/` | Integração com o **Groq** (intenção → critérios; rerank + justificativas) |
| `catalog-service/` | Integração com o **TMDB** (catálogo e "onde assistir") |
| `feedback-service/` | Feedback do usuário e métricas (C3) |

Linguagem padrão: **Java 21 + Spring Boot 4**. Outras linguagens são permitidas desde que o serviço cumpra seu contrato em `contracts/` e as convenções da seção 3 da arquitetura.

## Como rodar (sem Docker, por enquanto)

1. Copie `.env.example` para `.env` e preencha `TMDB_API_KEY` e `GROQ_API_KEY`.
2. Em três terminais, um por serviço:

```bash
cd backend/catalog-service && ./mvnw spring-boot:run          # porta 8081
cd backend/llm-service && ./mvnw spring-boot:run              # porta 8083
cd backend/recommendation-service && ./mvnw spring-boot:run   # porta 8082
```

3. No frontend: `npm run dev:backend`.

Sem `GROQ_API_KEY` o site continua funcionando, só que sem a IA: a resposta sai com `degraded: true`
e os títulos vêm na ordem do catálogo.

Testes de cada serviço: `./mvnw test` dentro da pasta dele.

## Ainda falta

- `gateway/` (Nginx) e `docker-compose.yml`;
- circuit breaker (Resilience4j) no recommendation-service; hoje há só timeout;
- no catalog: `keywords`, `originalLanguage` e `runtimeMin` ainda não viram filtros no TMDB, e `/search` e `/similar`
  (para "parecido com X") não existem.
