package br.com.watchmatch.catalog.tmdb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Um filme ou série da lista do TMDB.
// Filme usa title/release_date; série usa name/first_air_date. O outro par vem null.
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbResult(
        Integer id,
        String title,
        String name,
        @JsonProperty("original_title") String originalTitle,
        @JsonProperty("original_name") String originalName,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("first_air_date") String firstAirDate,
        @JsonProperty("vote_average") Double voteAverage,
        @JsonProperty("genre_ids") List<Integer> genreIds,
        @JsonProperty("original_language") String originalLanguage) {
}
