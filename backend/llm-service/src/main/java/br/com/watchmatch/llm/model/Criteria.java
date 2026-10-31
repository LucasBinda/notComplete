package br.com.watchmatch.llm.model;

import java.util.List;

// Critérios de busca (common.yaml). Campo null = "sem restrição" (e some do JSON)
public record Criteria(
        String mediaType,
        List<Integer> genres,
        List<Integer> excludeGenres,
        Integer runtimeMin,
        Integer runtimeMax,
        Integer yearFrom,
        Integer yearTo,
        String originalLanguage,
        String certificationMax,
        List<String> keywords) {

    public static Criteria empty() {
        return new Criteria(null, null, null, null, null, null, null, null, null, null);
    }
}
