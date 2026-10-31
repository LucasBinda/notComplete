package br.com.watchmatch.recommendation.model;

import java.util.List;

// Critérios de busca (common.yaml). Campo null ou lista vazia = "sem restrição".
// mediaType: "movie" ou "tv"; certificationMax: "L", "10", "12", "14", "16" ou "18"
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
