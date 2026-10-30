# WatchMatch (`notComplete`)

Aplicação web de **recomendação de filmes e séries** com filtros + intenção em linguagem natural (LLM via Groq), considerando as plataformas disponíveis para o usuário (ex.: Netflix, Amazon Prime Video).

Projeto da disciplina **Projeto Integrador IV (2026/2)** — FAESA · IA + Testes de Software.

## Grupo

- Lucas Binda
- Felipe Koscky

## Entrega 1 (C1) — status

Relatório com escopo, justificativa, metodologia, plano de trabalho e comunidade impactada:

- **[docs/RELATORIO_C1.md](docs/RELATORIO_C1.md)** — relatório completo da Entrega 1
- [docs/PLANO_TRABALHO.md](docs/PLANO_TRABALHO.md) — cronograma C1→C3
- [docs/IA_RESPONSAVEL.md](docs/IA_RESPONSAVEL.md) — privacidade, segurança e governança (inicial)
- [docs/ARQUITETURA.md](docs/ARQUITETURA.md) — stack, arquitetura, estrutura de pastas, telas e plano do C2

## Ideia em uma frase

Você descreve o que quer assistir (ou usa filtros); a IA interpreta e recomenda títulos **ancorados em catálogo público**, indicando **onde assistir** — sem depender dos filtros limitados de cada streaming.

## Como funciona

1. **Você descreve.** Escreva do seu jeito ou use os filtros: gênero, duração, formato e as plataformas que você assina.
2. **A IA interpreta.** Um modelo de linguagem transforma o pedido em critérios e escolhe, entre títulos reais do catálogo, os que mais combinam.
3. **Você vê onde assistir.** O site mostra **10 sugestões por vez** (o botão "Carregar mais" traz as próximas), cada uma com o motivo da escolha e as plataformas onde está disponível no Brasil.

Sem cadastro: as plataformas escolhidas ficam salvas só no navegador. A IA nunca inventa títulos, ela só escolhe entre itens reais do catálogo.

## Stack prevista

| Camada | Tecnologia (prevista) |
| ------ | --------------------- |
| Frontend | Web (React / Next.js) — mobile via navegador; app nativo como extra |
| Backend | Node.js ou Python |
| IA | GroqCloud (LLM) + prompts / saída estruturada; RAG se necessário |
| Dados | TMDB (metadados) + API de disponibilidade (ex.: Watchmode) |
| Deploy | Render ou Railway (C3) |

> Catálogos oficiais da Netflix/Prime **não** serão baixados nem scrapados (ToS / direitos). Usamos fontes de metadados e “onde assistir” legítimas.

## Estrutura do repositório

```
frontend/   # UI web (em desenvolvimento)
backend/    # API + integração com IA (em desenvolvimento)
docs/       # Relatórios e documentação do projeto
```

## Checkpoints

| Checkpoint | Prazo | Artefato |
| ---------- | ----- | -------- |
| C1 Relatório | 18/09/2026 | Este README + `docs/RELATORIO_C1.md` |
| C2 Protótipo | 30/10/2026 | Vídeo YouTube + IA funcionando |
| C3 MVP | 04/12/2026 | Código, testes, eval, deploy, impacto |

## Licença

Ver [LICENSE](LICENSE).
