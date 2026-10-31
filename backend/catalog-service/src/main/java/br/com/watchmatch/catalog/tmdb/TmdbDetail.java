package br.com.watchmatch.catalog.tmdb;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.watchmatch.catalog.domain.Genre;

// Detalhes de /movie/{id} ou /tv/{id} com append_to_response (onde assistir, elenco e classificação).
// Como no TmdbResult, filme e série usam nomes diferentes; o par que não se aplica vem null.
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbDetail(
        Integer id,
        String title,
        String name,
        @JsonProperty("original_title") String originalTitle,
        @JsonProperty("original_name") String originalName,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("first_air_date") String firstAirDate,
        @JsonProperty("vote_average") Double voteAverage,
        List<Genre> genres,
        Integer runtime,
        @JsonProperty("episode_run_time") List<Integer> episodeRunTime,
        @JsonProperty("number_of_seasons") Integer numberOfSeasons,
        @JsonProperty("original_language") String originalLanguage,
        @JsonProperty("watch/providers") WatchProviders watchProviders,
        @JsonProperty("release_dates") ReleaseDates releaseDates,
        @JsonProperty("content_ratings") ContentRatings contentRatings,
        Credits credits) {

    // "watch/providers": { "results": { "BR": {...}, "US": {...} } }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WatchProviders(Map<String, CountryWatch> results) {
    }

    // Uma lista por tipo de oferta: assinatura, grátis, com anúncios, aluguel e compra
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CountryWatch(
            String link,
            List<TmdbProvider> flatrate,
            List<TmdbProvider> free,
            List<TmdbProvider> ads,
            List<TmdbProvider> rent,
            List<TmdbProvider> buy) {
    }

    // Classificação de filme: por país, uma lista de lançamentos (cinema, digital...)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReleaseDates(List<CountryReleases> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CountryReleases(
            @JsonProperty("iso_3166_1") String country,
            @JsonProperty("release_dates") List<Release> releaseDates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Release(String certification) {
    }

    // Classificação de série: uma por país
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentRatings(List<ContentRating> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentRating(@JsonProperty("iso_3166_1") String country, String rating) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Credits(List<Cast> cast) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cast(String name) {
    }
}
