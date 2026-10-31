package br.com.watchmatch.catalog.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

// No JSON do contrato os valores vão em minúsculas
public enum TitleType {
    @JsonProperty("movie") MOVIE,
    @JsonProperty("tv") TV
}
