package br.com.watchmatch.catalog.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Uma plataforma de streaming do jeito que o TMDB manda
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbProvider(
        @JsonProperty("provider_id") Integer providerId,
        @JsonProperty("provider_name") String providerName,
        @JsonProperty("logo_path") String logoPath) {
}
