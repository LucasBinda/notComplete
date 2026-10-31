package br.com.watchmatch.catalog.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Aponta para um título: tipo + id do TMDB
public record TitleRef(@NotNull TitleType mediaType, @NotNull @Min(1) Integer id) {
}
