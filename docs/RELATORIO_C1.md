# Entrega 1 (C1) — Relatório do Projeto

**Disciplina:** Projeto Integrador IV (2026/2) — Aplicações de Inteligência Artificial  
**Checkpoint:** C1 — Relatório (prazo 18/09/2026)  
**Repositório:** [github.com/LucasBinda/notComplete](https://github.com/LucasBinda/notComplete)

---

## 1. Identificação do grupo

| Integrante | Papel previsto |
| ---------- | -------------- |
| Lucas Binda | Organização do repositório, backend/API e integração com LLM |
| Felipe Koscky | Frontend web, UX dos filtros e documentação |

Trabalho em dupla (limite do edital: até 5 integrantes).

---

## 2. Área temática

**Tecnologia e Inovação**, com recorte em **entretenimento digital** e **experiência do usuário** em serviços de streaming.

Recortes previstos ao longo do semestre (evolução, não escopo mínimo do C1): apoio a famílias (conteúdo por faixa etária) e acessibilidade (legendas / audiodescrição quando houver dados).

---

## 3. Escopo do projeto

### 3.1 Nome provisório

**WatchMatch** — recomendador web de filmes e séries com base em filtros explícitos e intenção em linguagem natural, considerando as plataformas disponíveis para o usuário (ex.: Netflix, Amazon Prime Video e outras, conforme dados de “onde assistir”).

### 3.2 Problema

As plataformas de streaming oferecem catálogos enormes, porém os filtros nativos são limitados: pouco flexíveis, pouco expressivos em linguagem cotidiana (“quero algo leve para casal, até duas horas, sem terror”) e fragmentados entre apps. O resultado é **fadiga de escolha**, tempo perdido navegando e baixa satisfação ao “só querer achar o que assistir”.

### 3.3 Solução proposta

Aplicação **web** (prioridade: funcionar em desktop e mobile via navegador) com:

1. **Filtros estruturados** — plataforma(s), gênero, duração, ano, classificação etária, idioma, humor/tema etc.
2. **Entrada em linguagem natural** — o usuário descreve o que quer; um **LLM** (via API Groq, free tier) interpreta a intenção e gera critérios estruturados.
3. **Catálogo e disponibilidade** — metadados via APIs públicas/legítimas (ex.: **TMDB** para títulos/sinopses; **Watchmode** ou equivalente para “onde assistir” no Brasil). **Não** haverá scraping nem cópia integral de bases proprietárias da Netflix ou da Amazon (restrições de ToS e direitos autorais).
4. **Explicabilidade leve** — a IA justifica, em texto curto, por que cada título foi sugerido.
5. **Extra (pós-MVP)** — PWA ou aplicativo nativo, se o tempo permitir após o MVP web.

### 3.4 Fora de escopo (C1–C3)

- Reproduzir vídeo dentro do app (apenas recomendação + link/indicação da plataforma).
- Contas oficiais Netflix/Prime ou login OAuth nessas plataformas.
- Treinar modelo de recomendação do zero (usaremos LLM + recuperação sobre catálogo).
- Cobertura exaustiva de todos os streamings do mundo no MVP (foco em principais disponíveis no BR).

### 3.5 Requisitos técnicos alinhados ao edital

| Requisito | Como atenderemos |
| --------- | ---------------- |
| Backend + frontend | API (Node ou Python) + frontend web (React/Next.js) |
| Componente de IA | LLM via **Groq**; prompts + parsing de intenção; quando fizer sentido, RAG sobre sinopses/tags |
| Testes | Testes unitários/integração nas partes críticas (filtros, parsing, API) |
| Eval de IA | Harness simples: relevância, disponibilidade na plataforma pedida, taxa de resposta inválida/alucinação |
| Deploy | MVP em free tier (Render/Railway) com URL pública (C3) |
| IA responsável | LGPD, minimização de dados, mitigação de prompt injection (ver `docs/IA_RESPONSAVEL.md`) |

---

## 4. Justificativa do tema

1. **Problema real e recorrente:** escolha de conteúdo em streaming é cotidiana e mal suportada pelos filtros nativos.
2. **Encaixa em IA aplicada:** o valor não é “mais um filtro SQL”, e sim interpretar intenção ambígua, combinar critérios e explicar recomendações — uso típico de LLM + dados estruturados.
3. **Viabilidade com custo zero:** Groq (LLM), TMDB e free tiers de hospedagem permitem MVP sem orçamento.
4. **Extensão:** a solução mira uma comunidade concreta de usuários de streaming no Brasil, com indicadores mensuráveis de impacto (tempo até achar título, aceitação da sugestão etc.).
5. **ODS relacionados:** ODS 9 (inovação), ODS 12 (consumo consciente de tempo/atenção) e, na evolução, ODS 4/3 (conteúdo adequado e bem-estar digital em famílias).

---

## 5. Metodologia

Abordagem **iterativa**, alinhada aos checkpoints C1–C3:

### 5.1 Levantamento e definição (C1)

- Caracterizar o problema e a comunidade.
- Definir escopo, fontes de dados legítimas e indicadores.
- Estruturar repositório e documentação inicial.

### 5.2 Prototipagem do componente de IA (C2)

- Implementar fluxo: texto do usuário → prompt → LLM (Groq) → critérios JSON → consulta ao catálogo.
- Validar viabilidade técnica (respostas úteis, latência aceitável, limites do free tier).
- Primeiros testes automatizados e **eval** inicial (conjunto pequeno de casos: “filme curto de comédia no Prime”, “terror anos 80 no Netflix”, etc.).
- Vídeo no YouTube demonstrando o protótipo.

### 5.3 MVP, testes, deploy e impacto (C3)

- Interface web completa (filtros + chat/caixa de intenção + lista de recomendações).
- Suite de testes + harness de eval documentado.
- Deploy com URL pública.
- Análise de impacto com os indicadores definidos; revisão de IA responsável.

### 5.4 Engenharia de contexto prevista

- **Prompts** versionados no repositório (conforme edital: registrar uso de IA no desenvolvimento).
- **Function/tool calling** (quando útil) para forçar saída estruturada (filtros JSON).
- **RAG** opcional sobre sinopses/metadados para reduzir alucinações de títulos inexistentes.

---

## 6. Plano de trabalho

| Período | Entrega | Atividades principais | Responsáveis |
| ------- | ------- | --------------------- | ------------ |
| Até **18/09/2026** | **C1** | Relatório, README, estrutura do repo | Lucas + Felipe |
| 19/09 – 15/10 | Desenvolvimento | API catálogo (TMDB + disponibilidade); frontend de filtros; integração Groq | Lucas (API/IA), Felipe (UI) |
| Até **30/10/2026** | **C2** | Protótipo com IA funcionando; eval inicial; vídeo YouTube | Lucas + Felipe |
| 01/11 – 25/11 | Refino | Testes, UX, RAG/explicações, hardening (prompt injection, LGPD) | Lucas + Felipe |
| Até **04/12/2026** | **C3** | MVP deployado; docs; testes + eval; vídeo; análise de impacto | Lucas + Felipe |

Marcos internos sugeridos: integração Groq estável até meados de outubro; UI usável uma semana antes do C2; checklist de deploy duas semanas antes do C3.

---

## 7. Comunidade impactada

### 7.1 Quem é

**Assinantes de serviços de streaming no Brasil** — em especial jovens e adultos (18–45 anos) e núcleos familiares — que consomem Netflix, Amazon Prime Video e plataformas semelhantes, mas enfrentam dificuldade para escolher o que assistir com os filtros nativos.

Não é necessário que a comunidade “participe” do desenvolvimento (conforme edital); ela é **caracterizada e analisada** como público-alvo do impacto.

### 7.2 Problema social/prático

- Tempo e atenção desperdiçados na navegação.
- Frustração e abandono da sessão (“não achei nada”).
- Fragmentação: o título desejado pode estar em outra plataforma já assinada, sem o usuário perceber.

### 7.3 Indicadores de impacto previstos

| Indicador | Tipo | Como medir (previsto) |
| --------- | ---- | --------------------- |
| Taxa de aceitação da sugestão | Quantitativo | % de sessões em que o usuário marca “vou assistir” / feedback positivo |
| Tempo até achar um título aceitável | Quantitativo | Tempo entre início da busca e primeira aceitação (ou proxy em teste de usabilidade) |
| Adequação à plataforma pedida | Quantitativo | % de recomendações com disponibilidade na(s) plataforma(s) filtrada(s) |
| Satisfação subjetiva | Qualitativo/quantitativo | Escala 1–5 pós-uso (formulário curto) |
| Redução de “não achei nada” | Quantitativo | % de sessões sem nenhuma recomendação útil vs. baseline relatado pelo usuário |

Fontes de contexto para caracterização da comunidade (a aprofundar no C3): dados públicos sobre consumo de streaming no Brasil (IBGE/pesquisa de TIC, reportagens setoriais, documentação TMDB/Watchmode sobre cobertura de catálogo).

### 7.4 Evolução do impacto (C2/C3)

Incluir filtros de **faixa etária** e, se houver dados, **acessibilidade** (legendas / audiodescrição), ampliando o benefício para famílias e pessoas com necessidades específicas de acesso ao conteúdo.

---

## 8. Organização inicial do GitHub

Estrutura na entrega C1:

```
notComplete/
├── README.md
├── LICENSE
├── .gitignore
├── docs/
│   ├── RELATORIO_C1.md      ← este documento
│   ├── PLANO_TRABALHO.md
│   └── IA_RESPONSAVEL.md
├── frontend/                ← aplicação web (a desenvolver)
└── backend/                 ← API + integração IA (a desenvolver)
```

Próximos passos no repositório: código do MVP, prompts versionados, testes, eval e instruções de deploy. Inclusão do professor como colaborador será feita assim que o username GitHub for confirmado.

---

## 9. Riscos e mitigações

| Risco | Mitigação |
| ----- | --------- |
| Limites do free tier Groq | Cache de intenções comuns; prompts curtos; fallback de filtros só manuais |
| Catálogo incompleto / “onde assistir” desatualizado | Deixar explícito na UI; priorizar plataformas principais; documentar fonte |
| Alucinação de títulos | Restringir recomendações ao resultado da busca no catálogo (RAG/grounding) |
| Escopo grande demais para dupla | MVP web enxuto; app nativo só como extra |

---

*Documento referente à Entrega 1 (C1) — Projeto Integrador IV · FAESA · 2026/2.*
