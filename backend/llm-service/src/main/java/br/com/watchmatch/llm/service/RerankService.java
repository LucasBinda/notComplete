package br.com.watchmatch.llm.service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.com.watchmatch.llm.groq.GroqClient;
import br.com.watchmatch.llm.groq.GroqClient.Completion;
import br.com.watchmatch.llm.model.LlmMeta;
import br.com.watchmatch.llm.model.Rerank;
import br.com.watchmatch.llm.model.Rerank.Candidate;
import br.com.watchmatch.llm.model.Rerank.Pick;

// POST /rerank: a IA escolhe os melhores entre candidatos reais e justifica cada escolha
@Service
public class RerankService {

    static final String PROMPT_VERSION = "rerank.v1";

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_REASON = 200;

    private final GroqClient groq;
    private final PromptLoader prompts;
    private final String model;

    public RerankService(GroqClient groq, PromptLoader prompts, @Value("${llm.rerank-model}") String model) {
        this.groq = groq;
        this.prompts = prompts;
        this.model = model;
    }

    public Rerank.Response rerank(Rerank.Request request) {
        long start = System.nanoTime();
        int limit = request.limit() != null ? request.limit() : DEFAULT_LIMIT;

        Completion completion = groq.complete(model, prompts.load(PROMPT_VERSION), userMessage(request, limit),
                "rerank", schema(request.candidates()));
        Answer answer = Prompts.parse(completion.content(), Answer.class);
        List<Pick> raw = answer.picks() == null ? List.of() : answer.picks();

        // Grounding: só passa o que está na lista de candidatos, uma vez cada
        Set<String> real = request.candidates().stream()
                .map(c -> key(c.mediaType(), c.id()))
                .collect(Collectors.toSet());
        Set<String> used = new HashSet<>();
        List<Pick> valid = raw.stream()
                .filter(p -> real.contains(key(p.mediaType(), p.id())))
                .filter(p -> used.add(key(p.mediaType(), p.id())))
                .toList();
        int discarded = raw.size() - valid.size();

        List<Pick> picks = valid.stream()
                .limit(limit)
                .map(p -> new Pick(p.id(), p.mediaType(), shorten(p.reason())))
                .toList();
        var meta = new LlmMeta(model, PROMPT_VERSION, Prompts.elapsedMs(start), completion.totalTokens());
        return new Rerank.Response(picks, discarded, meta);
    }

    private String userMessage(Rerank.Request request, int limit) {
        String summary = request.summary() == null || request.summary().isBlank()
                ? "" : "Resumo do pedido: " + request.summary() + "\n\n";
        return "Escolha no máximo " + limit + " títulos.\n\n"
                + summary
                + Prompts.userBlock(request.text())
                + "\n\nCandidatos (JSON):\n" + Prompts.JSON.writeValueAsString(request.candidates());
    }

    private Map<String, Object> schema(List<Candidate> candidates) {
        // O enum de ids faz a IA só conseguir escrever ids dos candidatos
        List<Integer> ids = candidates.stream().map(Candidate::id).distinct().toList();
        Map<String, Object> pickProperties = new LinkedHashMap<>();
        pickProperties.put("id", Map.of("type", "integer", "enum", ids));
        pickProperties.put("mediaType", Map.of("type", "string", "enum", List.of("movie", "tv")));
        pickProperties.put("reason", Map.of("type", "string"));

        Map<String, Object> pick = Map.of(
                "type", "object",
                "properties", pickProperties,
                "required", List.copyOf(pickProperties.keySet()),
                "additionalProperties", false);
        return Map.of(
                "type", "object",
                "properties", Map.of("picks", Map.of("type", "array", "items", pick)),
                "required", List.of("picks"),
                "additionalProperties", false);
    }

    private String shorten(String reason) {
        if (reason == null) {
            return "";
        }
        String trimmed = reason.trim();
        return trimmed.length() > MAX_REASON ? trimmed.substring(0, MAX_REASON - 1) + "…" : trimmed;
    }

    private static String key(String mediaType, Integer id) {
        return mediaType + "-" + id;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Answer(List<Pick> picks) {
    }
}
