package br.com.watchmatch.catalog.tmdb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Resposta do /watch/providers/movie do TMDB
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbProviderList(List<TmdbProvider> results) {
}
