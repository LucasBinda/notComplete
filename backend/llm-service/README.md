# llm-service

Único serviço que conversa com o LLM (**Groq**). Sem rota pública: só o recommendation-service o chama.
Contrato: [`contracts/llm-service.yaml`](../contracts/llm-service.yaml).

**Linguagem:** Java 21 + Spring Boot 4 (padrão do projeto).

## Rotas

| Rota | O que faz |
| ---- | --------- |
| `POST /intent` | Texto do usuário → critérios de busca |
| `POST /rerank` | Escolhe os melhores entre candidatos reais e justifica cada um |
| `GET /health` | Saúde |

## Como a saída da IA é controlada

- **Structured outputs em modo estrito:** o Groq só pode responder JSON no schema enviado. Os ids de gênero,
  plataforma e candidato vão como `enum`, então a IA não consegue escrever um id que não existe.
- **Validação depois:** mesmo assim, tudo é conferido (ids, anos, classificação, tamanho dos textos), e o
  `/rerank` descarta escolhas fora dos candidatos. O total descartado volta em `discarded`.
- **Texto do usuário isolado:** vai entre `<pedido>` e `</pedido>`, e o prompt manda tratá-lo só como pedido.
- **Prompts versionados** em `src/main/resources/prompts/`; a versão usada volta em `meta.promptVersion`.

## Como rodar e testar

```bash
./mvnw spring-boot:run   # porta 8083; lê GROQ_API_KEY de ../.env
./mvnw test              # usa um Groq falso, não gasta a cota
```

| Variável | Padrão |
| -------- | ------ |
| `GROQ_API_KEY` | — |
| `LLM_INTENT_MODEL` | `openai/gpt-oss-20b` |
| `LLM_RERANK_MODEL` | `openai/gpt-oss-120b` |
