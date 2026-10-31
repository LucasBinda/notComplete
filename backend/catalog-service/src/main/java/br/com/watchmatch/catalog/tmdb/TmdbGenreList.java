package br.com.watchmatch.catalog.tmdb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.com.watchmatch.catalog.domain.Genre;

// Resposta do /genre/{movie|tv}/list do TMDB
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbGenreList(List<Genre> genres) {
}
