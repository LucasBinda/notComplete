# recommendation-service

Orquestra uma recomendação: texto → IA entende → mescla com os filtros → catálogo busca candidatos
reais → IA escolhe e justifica → grounding → detalhes. Contrato:
[`contracts/recommendation-service.yaml`](../contracts/recommendation-service.yaml).

**Linguagem:** Java 21 + Spring Boot 4 (padrão do projeto).

## Regras importantes

- O filtro da tela sempre vence o que a IA entendeu (`CriteriaMerger`).
- **Grounding:** só aparecem títulos que vieram do catálogo; id inventado pela IA é descartado.
- Se o llm-service falhar, a resposta sai com os títulos do catálogo e `degraded: true`.
  Se o catálogo falhar, a resposta é `502` (sem catálogo não há recomendação).
- O `X-Request-Id` é repassado para os outros serviços e aparece nos logs.

## Como rodar e testar

```bash
./mvnw spring-boot:run   # porta 8082
./mvnw test
```

| Variável | Padrão |
| -------- | ------ |
| `CATALOG_URL` | `http://localhost:8081` |
| `LLM_URL` | `http://localhost:8083` |
