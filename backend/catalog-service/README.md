# catalog-service

Integração com o **TMDB**: busca de títulos, detalhes, onde assistir no Brasil, gêneros e plataformas.
Contrato: [`contracts/catalog-service.yaml`](../contracts/catalog-service.yaml).

**Linguagem:** Java 21 + Spring Boot 4 (padrão do projeto).

## Rotas

| Rota | O que faz |
| ---- | --------- |
| `GET /meta` | Gêneros (filme e série) e as plataformas oferecidas no site |
| `POST /discover` | Títulos reais que atendem aos critérios, nas plataformas do usuário |
| `GET /titles/{movie\|tv}/{id}` | Detalhes + onde assistir + elenco + classificação |
| `POST /titles/lookup` | Detalhes de até 20 títulos de uma vez |
| `GET /health` | Saúde |

## Como rodar e testar

```bash
./mvnw spring-boot:run   # porta 8081; lê TMDB_API_KEY de ../.env
./mvnw test
```

| Variável | Padrão | Para quê |
| -------- | ------ | -------- |
| `TMDB_API_KEY` | — | Token de leitura (v4) do TMDB |
| `CATALOG_PROVIDERS` | `8,119,337,1899,307,350,531` | Plataformas do site, em ordem de relevância |

Os testes não usam a internet: um TMDB falso (`MockRestServiceServer`) responde no lugar do real.
