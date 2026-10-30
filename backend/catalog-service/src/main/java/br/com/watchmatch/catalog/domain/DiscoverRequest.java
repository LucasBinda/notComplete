package br.com.watchmatch.catalog.domain;

import java.util.List;

public record DiscoverRequest(Criteria criteria, List<Integer> providers, SortBy sortBy) {

    // Atalho para quem não escolhe ordenação: usa popularidade
    public DiscoverRequest(Criteria criteria, List<Integer> providers) {
        this(criteria, providers, SortBy.POPULARITY);
    }
}
