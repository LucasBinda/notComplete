package br.com.watchmatch.catalog.domain;

// id é o mesmo do TMDB (ex.: 8 = Netflix, 119 = Amazon Prime Video)
public record Provider(Integer id, String name, String logoUrl) {
}
