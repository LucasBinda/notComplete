package br.com.watchmatch.catalog.domain;

import java.util.List;

// Mesmo formato do TitleDetail em contracts/catalog-service.yaml (TitleSummary + detalhes)
public record TitleDetail(
        Integer id,
        TitleType mediaType,
        String title,
        String originalTitle,
        Integer year,
        String overview,
        String posterUrl,
        List<Genre> genres,
        Double voteAverage,
        String originalLanguage,
        Integer runtime,
        Integer seasons,
        String certification,
        String backdropUrl,
        List<String> cast,
        WatchInfo watch) {
}
