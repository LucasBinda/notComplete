package br.com.watchmatch.catalog.tmdb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Uma página de resultados do /discover do TMDB, do jeito que ele manda
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbPage(
        Integer page,
        @JsonProperty("total_pages") Integer totalPages,
        List<TmdbResult> results) {
}
