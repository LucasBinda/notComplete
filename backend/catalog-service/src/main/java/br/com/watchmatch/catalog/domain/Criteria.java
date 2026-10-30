package br.com.watchmatch.catalog.domain;

import java.util.List;

// Campos Integer/String podem ser null = "sem restrição"
public record Criteria(
        List<Integer> genres,
        List<Integer> excludeGenres,
        Integer runtimeMax,
        Integer yearFrom,
        Integer yearTo,
        String certificationMax) {

    // Roda sempre que uma Criteria é criada: lista null vira lista vazia
    public Criteria {
        genres = genres == null ? List.of() : genres;
        excludeGenres = excludeGenres == null ? List.of() : excludeGenres;
    }

    public static Criteria empty() {
        return new Criteria(List.of(), List.of(), null, null, null, null);
    }

    // Cada with... devolve uma cópia da ficha com um único espaço trocado.
    // Records não podem ser alterados depois de criados, por isso é sempre uma ficha nova.

    public Criteria withGenres(List<Integer> genres) {
        return new Criteria(genres, excludeGenres, runtimeMax, yearFrom, yearTo, certificationMax);
    }

    public Criteria withExcludeGenres(List<Integer> excludeGenres) {
        return new Criteria(genres, excludeGenres, runtimeMax, yearFrom, yearTo, certificationMax);
    }

    public Criteria withRuntimeMax(Integer runtimeMax) {
        return new Criteria(genres, excludeGenres, runtimeMax, yearFrom, yearTo, certificationMax);
    }

    public Criteria withYearFrom(Integer yearFrom) {
        return new Criteria(genres, excludeGenres, runtimeMax, yearFrom, yearTo, certificationMax);
    }

    public Criteria withYearTo(Integer yearTo) {
        return new Criteria(genres, excludeGenres, runtimeMax, yearFrom, yearTo, certificationMax);
    }

    public Criteria withCertificationMax(String certificationMax) {
        return new Criteria(genres, excludeGenres, runtimeMax, yearFrom, yearTo, certificationMax);
    }
}
