package br.com.watchmatch.catalog.domain;

import java.util.List;

// mediaType null = filmes e séries; page null = primeira página
public record DiscoverRequest(
        Criteria criteria,
        List<Integer> providers,
        SortBy sortBy,
        TitleType mediaType,
        Integer page) {

    // Atalho para quem não escolhe ordenação: usa popularidade
    public DiscoverRequest(Criteria criteria, List<Integer> providers) {
        this(criteria, providers, SortBy.POPULARITY);
    }

    public DiscoverRequest(Criteria criteria, List<Integer> providers, SortBy sortBy) {
        this(criteria, providers, sortBy, null, 1);
    }
}
