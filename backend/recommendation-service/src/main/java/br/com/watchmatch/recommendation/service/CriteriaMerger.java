package br.com.watchmatch.recommendation.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import br.com.watchmatch.recommendation.model.Criteria;
import br.com.watchmatch.recommendation.model.Llm.IntentResponse;

// Junta os filtros da tela com o que a IA entendeu. Regra: o filtro da tela sempre vence.
@Component
public class CriteriaMerger {

    public MergedCriteria merge(Criteria filters, IntentResponse intent, List<Integer> userProviders) {
        Criteria f = filters != null ? filters : Criteria.empty();
        Criteria ai = intent != null && intent.criteria() != null ? intent.criteria() : Criteria.empty();
        Map<String, String> sources = new LinkedHashMap<>();

        List<Integer> genres = pick("genres", f.genres(), ai.genres(), sources);
        List<Integer> excludeGenres = pick("excludeGenres", f.excludeGenres(), ai.excludeGenres(), sources);
        // Gênero pedido nunca pode ficar ao mesmo tempo na lista de proibidos
        if (excludeGenres != null && genres != null) {
            excludeGenres = excludeGenres.stream().filter(g -> !genres.contains(g)).toList();
        }

        Criteria merged = new Criteria(
                pick("mediaType", f.mediaType(), ai.mediaType(), sources),
                genres,
                excludeGenres,
                pick("runtimeMin", f.runtimeMin(), ai.runtimeMin(), sources),
                pick("runtimeMax", f.runtimeMax(), ai.runtimeMax(), sources),
                pick("yearFrom", f.yearFrom(), ai.yearFrom(), sources),
                pick("yearTo", f.yearTo(), ai.yearTo(), sources),
                pick("originalLanguage", f.originalLanguage(), ai.originalLanguage(), sources),
                pick("certificationMax", f.certificationMax(), ai.certificationMax(), sources),
                pick("keywords", f.keywords(), ai.keywords(), sources));

        return new MergedCriteria(merged, providers(intent, userProviders, sources), sources);
    }

    // "Quero algo na Netflix": a IA cita plataformas; vale só as que o usuário assina
    private List<Integer> providers(IntentResponse intent, List<Integer> userProviders, Map<String, String> sources) {
        if (intent == null || intent.providers() == null || intent.providers().isEmpty()) {
            return userProviders;
        }
        List<Integer> cited = userProviders.stream().filter(intent.providers()::contains).toList();
        if (cited.isEmpty()) {
            return userProviders;
        }
        sources.put("providers", "ai");
        return cited;
    }

    private <T> T pick(String field, T fromFilter, T fromAi, Map<String, String> sources) {
        if (isSet(fromFilter)) {
            sources.put(field, "filter");
            return fromFilter;
        }
        if (isSet(fromAi)) {
            sources.put(field, "ai");
            return fromAi;
        }
        return fromFilter;
    }

    private boolean isSet(Object value) {
        if (value instanceof List<?> list) {
            return !list.isEmpty();
        }
        if (value instanceof String text) {
            return !text.isBlank();
        }
        return value != null;
    }
}
