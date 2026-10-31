package br.com.watchmatch.catalog.domain;

import java.util.List;

// Mesmo formato do Meta em contracts/catalog-service.yaml
public record Meta(Genres genres, List<Provider> providers) {

    public record Genres(List<Genre> movie, List<Genre> tv) {
    }
}
