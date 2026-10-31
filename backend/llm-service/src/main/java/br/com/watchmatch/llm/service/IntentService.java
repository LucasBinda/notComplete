package br.com.watchmatch.llm.service;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.com.watchmatch.llm.groq.GroqClient;
import br.com.watchmatch.llm.groq.GroqClient.Completion;
import br.com.watchmatch.llm.model.Criteria;
import br.com.watchmatch.llm.model.Genre;
import br.com.watchmatch.llm.model.Intent;
import br.com.watchmatch.llm.model.LlmMeta;
import br.com.watchmatch.llm.model.Provider;

// POST /intent: texto do usuário → critérios de busca
@Service
public class IntentService {

    static final String PROMPT_VERSION = "intent.v1";

    private static final Set<String> CERTIFICATIONS = Set.of("L", "10", "12", "14", "16", "18");
    private static final Pattern LANGUAGE = Pattern.compile("[a-z]{2}");
    private static final int MAX_KEYWORDS = 5;
    private static final int MAX_KEYWORD_LENGTH = 50;
    private static final int MAX_SUMMARY = 120;

    private final GroqClient groq;
    private final PromptLoader prompts;
    private final String model;

    public IntentService(GroqClient groq, PromptLoader prompts, @Value("${llm.intent-model}") String model) {
        this.groq = groq;
        this.prompts = prompts;
        this.model = model;
    }

    public Intent.Response interpret(Intent.Request request) {
        long start = System.nanoTime();
        List<Integer> genreIds = request.genres().stream().map(Genre::id).toList();
        List<Integer> providerIds = request.providers().stream().map(Provider::id).toList();

        Completion completion = groq.complete(model, prompts.load(PROMPT_VERSION), userMessage(request),
                "intent", schema(genreIds, providerIds));
        Answer answer = Prompts.parse(completion.content(), Answer.class);
        var meta = new LlmMeta(model, PROMPT_VERSION, Prompts.elapsedMs(start), completion.totalTokens());

        if (Boolean.TRUE.equals(answer.offTopic())) {
            return new Intent.Response(Criteria.empty(), List.of(), null, summary(answer.summary()), true, meta);
        }

        // Mesmo com o modo estrito, conferimos tudo: a IA é tratada como uma fonte que pode errar
        var criteria = new Criteria(
                "movie".equals(answer.mediaType()) || "tv".equals(answer.mediaType()) ? answer.mediaType() : null,
                allowed(answer.genres(), genreIds),
                allowed(answer.excludeGenres(), genreIds),
                null,
                answer.runtimeMax() != null && answer.runtimeMax() > 0 ? answer.runtimeMax() : null,
                year(answer.yearFrom()),
                year(answer.yearTo()),
                answer.originalLanguage() != null && LANGUAGE.matcher(answer.originalLanguage()).matches()
                        ? answer.originalLanguage() : null,
                CERTIFICATIONS.contains(answer.certificationMax()) ? answer.certificationMax() : null,
                keywords(answer.keywords()));
        List<Integer> providers = answer.providers() == null ? List.of()
                : answer.providers().stream().filter(providerIds::contains).distinct().toList();
        String referenceTitle = answer.referenceTitle() == null || answer.referenceTitle().isBlank()
                ? null : answer.referenceTitle().trim();

        return new Intent.Response(criteria, providers, referenceTitle, summary(answer.summary()), false, meta);
    }

    private String userMessage(Intent.Request request) {
        return "Ano atual: " + Year.now().getValue() + "\n\n"
                + "Gêneros permitidos (id: nome):\n" + lines(request.genres().stream()
                        .map(g -> g.id() + ": " + g.name()).toList())
                + "\n\nPlataformas (id: nome):\n" + lines(request.providers().stream()
                        .map(p -> p.id() + ": " + p.name()).toList())
                + "\n\n" + Prompts.userBlock(request.text());
    }

    private String lines(List<String> items) {
        return String.join("\n", items);
    }

    // Modo estrito: todo campo é obrigatório e não aceita null.
    // "Não sei" vira 0, "", "any" ou lista vazia, e o serviço troca por null depois.
    private Map<String, Object> schema(List<Integer> genreIds, List<Integer> providerIds) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("offTopic", Map.of("type", "boolean"));
        properties.put("mediaType", Map.of("type", "string", "enum", List.of("movie", "tv", "any")));
        properties.put("genres", arrayOf(integerOf(genreIds)));
        properties.put("excludeGenres", arrayOf(integerOf(genreIds)));
        properties.put("runtimeMax", Map.of("type", "integer"));
        properties.put("yearFrom", Map.of("type", "integer"));
        properties.put("yearTo", Map.of("type", "integer"));
        properties.put("originalLanguage", Map.of("type", "string"));
        properties.put("certificationMax", Map.of("type", "string",
                "enum", List.of("", "L", "10", "12", "14", "16", "18")));
        properties.put("keywords", arrayOf(Map.of("type", "string")));
        properties.put("providers", arrayOf(integerOf(providerIds)));
        properties.put("referenceTitle", Map.of("type", "string"));
        properties.put("summary", Map.of("type", "string"));

        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.copyOf(properties.keySet()),
                "additionalProperties", false);
    }

    // O enum faz a IA só conseguir escrever ids que existem
    private Map<String, Object> integerOf(List<Integer> allowedIds) {
        return allowedIds.isEmpty() ? Map.of("type", "integer") : Map.of("type", "integer", "enum", allowedIds);
    }

    private Map<String, Object> arrayOf(Map<String, Object> items) {
        return Map.of("type", "array", "items", items);
    }

    private List<Integer> allowed(List<Integer> ids, List<Integer> allowedIds) {
        if (ids == null) {
            return null;
        }
        List<Integer> valid = ids.stream().filter(allowedIds::contains).distinct().toList();
        return valid.isEmpty() ? null : valid;
    }

    private Integer year(Integer year) {
        return year != null && year >= 1900 && year <= Year.now().getValue() + 1 ? year : null;
    }

    private List<String> keywords(List<String> keywords) {
        if (keywords == null) {
            return null;
        }
        List<String> valid = keywords.stream()
                .filter(k -> k != null && !k.isBlank() && k.length() <= MAX_KEYWORD_LENGTH)
                .map(String::trim)
                .distinct()
                .limit(MAX_KEYWORDS)
                .collect(Collectors.toList());
        return valid.isEmpty() ? null : valid;
    }

    private String summary(String summary) {
        if (summary == null) {
            return "";
        }
        String trimmed = summary.trim();
        return trimmed.length() > MAX_SUMMARY ? trimmed.substring(0, MAX_SUMMARY) : trimmed;
    }

    // O JSON que a IA devolve (segue o schema acima)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Answer(
            Boolean offTopic,
            String mediaType,
            List<Integer> genres,
            List<Integer> excludeGenres,
            Integer runtimeMax,
            Integer yearFrom,
            Integer yearTo,
            String originalLanguage,
            String certificationMax,
            List<String> keywords,
            List<Integer> providers,
            String referenceTitle,
            String summary) {
    }
}
