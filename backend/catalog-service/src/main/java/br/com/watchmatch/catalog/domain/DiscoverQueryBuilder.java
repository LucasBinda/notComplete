package br.com.watchmatch.catalog.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiscoverQueryBuilder {

    public Map<String, String> build(DiscoverRequest request, TitleType type) {
        Map<String, String> params = new HashMap<>();
        params.put("watch_region", "BR");
        params.put("with_watch_providers", joinWithPipe(request.providers()));
        params.put("with_watch_monetization_types", "flatrate|free|ads");
        params.put("language", "pt-BR");
        params.put("vote_count.gte", "50");

        Criteria criteria = request.criteria();

        if (!criteria.genres().isEmpty()) {
            params.put("with_genres", joinWithPipe(criteria.genres()));
        }

        if (!criteria.excludeGenres().isEmpty()) {
            params.put("without_genres", joinWithPipe(criteria.excludeGenres()));
        }

        if (criteria.runtimeMax() != null) {
            params.put("with_runtime.lte", String.valueOf(criteria.runtimeMax()));
        }

        // Filme e série guardam a data com nomes diferentes no TMDB
        String dateField = switch (type) {
            case MOVIE -> "primary_release_date";
            case TV -> "first_air_date";
        };

        if (criteria.yearFrom() != null) {
            params.put(dateField + ".gte", criteria.yearFrom() + "-01-01");
        }

        if (criteria.yearTo() != null) {
            params.put(dateField + ".lte", criteria.yearTo() + "-12-31");
        }

        // O /discover/tv do TMDB não aceita filtro de classificação, só o de filmes
        if (criteria.certificationMax() != null && type == TitleType.MOVIE) {
            params.put("certification_country", "BR");
            params.put("certification.lte", criteria.certificationMax());
        }

        // Sem ordenação escolhida (null), usa popularidade
        SortBy sortBy = request.sortBy() != null ? request.sortBy() : SortBy.POPULARITY;
        String sortField = switch (sortBy) {
            case POPULARITY -> "popularity";
            case RATING -> "vote_average";
            case RECENT -> dateField;
        };
        params.put("sort_by", sortField + ".desc");

        return params;
    }

    // Junta [35, 10749] em "35|10749" — no TMDB, "|" significa "OU"
    private String joinWithPipe(List<Integer> ids) {
        return ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("|"));
    }
}
