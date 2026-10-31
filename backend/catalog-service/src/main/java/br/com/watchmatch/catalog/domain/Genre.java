package br.com.watchmatch.catalog.domain;

// id é o mesmo do TMDB (ex.: 35 = Comédia)
public record Genre(Integer id, String name) {
}
