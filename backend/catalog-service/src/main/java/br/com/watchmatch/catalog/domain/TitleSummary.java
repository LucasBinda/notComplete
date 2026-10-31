package br.com.watchmatch.catalog.domain;

import java.util.List;

// Mesmo formato do TitleSummary em contracts/catalog-service.yaml.
// year e posterUrl podem ser null quando o TMDB não tem a informação.
public record TitleSummary(
        Integer id,
        TitleType mediaType,
        String title,
        String originalTitle,
        Integer year,
        String overview,
        String posterUrl,
        List<Genre> genres,
        Double voteAverage,
        String originalLanguage) {
}
