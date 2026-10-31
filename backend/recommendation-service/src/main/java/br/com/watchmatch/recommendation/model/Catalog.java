package br.com.watchmatch.recommendation.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

// Formatos do catalog-service.yaml que este serviço usa
public final class Catalog {

    private Catalog() {
    }

    public record DiscoverRequest(
            @JsonUnwrapped Criteria criteria,
            List<Integer> providers,
            Integer limit,
            Integer page) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TitleSummary(
            Integer id,
            String mediaType,
            String title,
            Integer year,
            String overview,
            List<Genre> genres) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TitleList(List<TitleSummary> items, Boolean hasMore) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TitleDetail(
            Integer id,
            String mediaType,
            String title,
            String originalTitle,
            Integer year,
            String overview,
            String posterUrl,
            String backdropUrl,
            List<Genre> genres,
            Double voteAverage,
            Integer runtime,
            Integer seasons,
            String certification,
            WatchInfo watch) {
    }

    public record TitleRef(String mediaType, Integer id) {
    }

    public record LookupRequest(List<TitleRef> titles) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LookupResult(List<TitleDetail> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(Genres genres, List<Provider> providers) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Genres(List<Genre> movie, List<Genre> tv) {
    }
}
