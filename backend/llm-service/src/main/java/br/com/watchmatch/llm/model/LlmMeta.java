package br.com.watchmatch.llm.model;

// Qual modelo e qual versão de prompt responderam, quanto demorou e quantos tokens gastou
public record LlmMeta(String model, String promptVersion, Long latencyMs, Integer totalTokens) {
}
