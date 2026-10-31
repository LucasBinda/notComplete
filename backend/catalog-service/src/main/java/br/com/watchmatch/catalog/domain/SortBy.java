package br.com.watchmatch.catalog.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

// No JSON do contrato os valores vão em minúsculas
public enum SortBy {
    @JsonProperty("popularity") POPULARITY,
    @JsonProperty("rating") RATING,
    @JsonProperty("recent") RECENT
}
