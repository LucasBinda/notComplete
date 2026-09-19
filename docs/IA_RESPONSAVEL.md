# IA responsável — notas iniciais (C1)

Documento vivo; será detalhado até o C3.

## Privacidade e LGPD

- **Dados previstos:** preferências de busca (filtros e texto digitado), feedback opcional (“gostei / não gostei”). Sem obrigação de cadastro no MVP.
- **Minimização:** não armazenar histórico sensível além do necessário para melhorar recomendações na sessão (ou com consentimento explícito, se houver conta no futuro).
- **Fontes de catálogo:** APIs de metadados (ex.: TMDB) e de disponibilidade (ex.: Watchmode). **Não** coletamos bases proprietárias via scraping de Netflix/Prime.
- **Base legal (orientação):** legítimo interesse / consentimento para preferências voluntárias — a formalizar se houver persistência de dados pessoais.

## Segurança

- Risco de **prompt injection** no campo de intenção em linguagem natural.
- Mitigações previstas: system prompt restrito; saída estruturada (JSON/schema); recomendações **somente** a partir do catálogo retornado pela API (sem inventar títulos); sanitização e limites de tamanho do input.

## Ética e viés

- Rankings e sinopses podem refletir vieses de popularidade/idioma.
- Comunicar limitações na UI (“sugestões baseadas em metadados públicos; disponibilidade pode estar desatualizada”).
- Evitar afirmações absolutas sobre qualidade artística ou adequação etária sem dado de classificação.

## Custo e sustentabilidade

- Uso do **Groq** no free tier: monitorar tokens/chamadas; preferir prompts curtos; cache quando fizer sentido.
- Sem cobrança ao usuário final no escopo acadêmico.

## Registro de uso de IA no desenvolvimento

Conforme o edital, prompts usados para gerar ou auxiliar código/documentação devem ser registrados no repositório (pasta a criar, ex.: `docs/prompts/`).
