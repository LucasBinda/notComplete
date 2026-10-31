package br.com.watchmatch.catalog.api;

import java.util.List;

import br.com.watchmatch.catalog.domain.Criteria;
import br.com.watchmatch.catalog.domain.DiscoverRequest;
import br.com.watchmatch.catalog.domain.SortBy;
import br.com.watchmatch.catalog.domain.TitleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

// O JSON do DiscoverRequest do contrato: tudo num nível só (critérios + plataformas + página)
public record DiscoverBody(
        TitleType mediaType,
        List<Integer> genres,
        List<Integer> excludeGenres,
        Integer runtimeMax,
        Integer yearFrom,
        Integer yearTo,
        String certificationMax,
        @NotEmpty List<Integer> providers,
        @Min(1) @Max(60) Integer limit,
        @Min(1) @Max(500) Integer page,
        SortBy sortBy) {

    private static final int DEFAULT_LIMIT = 40;

    public DiscoverRequest toDomain() {
        var criteria = new Criteria(genres, excludeGenres, runtimeMax, yearFrom, yearTo, certificationMax);
        return new DiscoverRequest(criteria, providers, sortBy, mediaType, page);
    }

    public int limitOrDefault() {
        return limit != null ? limit : DEFAULT_LIMIT;
    }
}
