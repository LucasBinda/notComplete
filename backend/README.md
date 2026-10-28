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

Linguagem padrão: **Java 21 + Spring Boot 3**. Outras linguagens são permitidas desde que o serviço cumpra seu contrato em `contracts/` e as convenções da seção 3 da arquitetura.

Implementação a partir do ciclo C1→C2.
