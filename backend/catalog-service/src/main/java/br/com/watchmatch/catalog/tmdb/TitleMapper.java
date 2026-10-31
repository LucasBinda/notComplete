package br.com.watchmatch.catalog.tmdb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.watchmatch.catalog.domain.Genre;
import br.com.watchmatch.catalog.domain.TitleDetail;
import br.com.watchmatch.catalog.domain.TitleSummary;
import br.com.watchmatch.catalog.domain.TitleType;
import br.com.watchmatch.catalog.domain.WatchInfo;
import br.com.watchmatch.catalog.domain.WatchProvider;
import br.com.watchmatch.catalog.domain.WatchUrlBuilder;
import br.com.watchmatch.catalog.tmdb.TmdbDetail.CountryWatch;

public class TitleMapper {

    // w500 = pôster com 500px de largura, bom para os cards
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w1280";
    private static final String LOGO_BASE_URL = "https://image.tmdb.org/t/p/w92";

    // Classificação indicativa brasileira; qualquer outro valor é ignorado
    private static final Set<String> CERTIFICATIONS = Set.of("L", "10", "12", "14", "16", "18");
    private static final int MAX_CAST = 10;

    private final WatchUrlBuilder watchUrlBuilder;

    public TitleMapper() {
        this(new WatchUrlBuilder());
    }

    public TitleMapper(WatchUrlBuilder watchUrlBuilder) {
        this.watchUrlBuilder = watchUrlBuilder;
    }

    public TitleSummary toSummary(TmdbResult tmdb, TitleType type, Map<Integer, String> genreNames) {
        // Filme e série guardam título e data com nomes diferentes no TMDB
        boolean movie = type == TitleType.MOVIE;
        String title = movie ? tmdb.title() : tmdb.name();
        String originalTitle = movie ? tmdb.originalTitle() : tmdb.originalName();
        String date = movie ? tmdb.releaseDate() : tmdb.firstAirDate();

        return new TitleSummary(
                tmdb.id(),
                type,
                title,
                originalTitle,
                yearOf(date),
                tmdb.overview(),
                imageUrl(POSTER_BASE_URL, tmdb.posterPath()),
                genresOf(tmdb.genreIds(), genreNames),
                tmdb.voteAverage(),
                tmdb.originalLanguage());
    }

    public TitleDetail toDetail(TmdbDetail tmdb, TitleType type) {
        boolean movie = type == TitleType.MOVIE;
        String title = movie ? tmdb.title() : tmdb.name();

        return new TitleDetail(
                tmdb.id(),
                type,
                title,
                movie ? tmdb.originalTitle() : tmdb.originalName(),
                yearOf(movie ? tmdb.releaseDate() : tmdb.firstAirDate()),
                tmdb.overview(),
                imageUrl(POSTER_BASE_URL, tmdb.posterPath()),
                tmdb.genres() == null ? List.of() : tmdb.genres(),
                tmdb.voteAverage(),
                tmdb.originalLanguage(),
                movie ? runtimeOf(tmdb.runtime()) : episodeRuntimeOf(tmdb.episodeRunTime()),
                movie ? null : tmdb.numberOfSeasons(),
                movie ? movieCertification(tmdb) : tvCertification(tmdb),
                imageUrl(BACKDROP_BASE_URL, tmdb.backdropPath()),
                castOf(tmdb),
                watchOf(tmdb, title));
    }

    // "2014-11-05" vira 2014; null ou "" viram null
    private Integer yearOf(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return Integer.valueOf(date.substring(0, 4));
    }

    private String imageUrl(String baseUrl, String path) {
        return path == null ? null : baseUrl + path;
    }

    // Ids que não estão no mapa de nomes são ignorados
    private List<Genre> genresOf(List<Integer> ids, Map<Integer, String> genreNames) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream()
                .filter(genreNames::containsKey)
                .map(id -> new Genre(id, genreNames.get(id)))
                .toList();
    }

    // O TMDB usa 0 quando não sabe a duração
    private Integer runtimeOf(Integer runtime) {
        return runtime == null || runtime == 0 ? null : runtime;
    }

    private Integer episodeRuntimeOf(List<Integer> episodeRunTime) {
        return episodeRunTime == null || episodeRunTime.isEmpty() ? null : runtimeOf(episodeRunTime.get(0));
    }

    // Um filme tem vários lançamentos no Brasil (cinema, digital...); vale a primeira classificação preenchida
    private String movieCertification(TmdbDetail tmdb) {
        if (tmdb.releaseDates() == null || tmdb.releaseDates().results() == null) {
            return null;
        }
        return tmdb.releaseDates().results().stream()
                .filter(country -> "BR".equals(country.country()) && country.releaseDates() != null)
                .flatMap(country -> country.releaseDates().stream())
                .map(TmdbDetail.Release::certification)
                .filter(CERTIFICATIONS::contains)
                .findFirst()
                .orElse(null);
    }

    private String tvCertification(TmdbDetail tmdb) {
        if (tmdb.contentRatings() == null || tmdb.contentRatings().results() == null) {
            return null;
        }
        return tmdb.contentRatings().results().stream()
                .filter(rating -> "BR".equals(rating.country()))
                .map(TmdbDetail.ContentRating::rating)
                .filter(CERTIFICATIONS::contains)
                .findFirst()
                .orElse(null);
    }

    private List<String> castOf(TmdbDetail tmdb) {
        if (tmdb.credits() == null || tmdb.credits().cast() == null) {
            return List.of();
        }
        return tmdb.credits().cast().stream().map(TmdbDetail.Cast::name).limit(MAX_CAST).toList();
    }

    private WatchInfo watchOf(TmdbDetail tmdb, String title) {
        CountryWatch br = tmdb.watchProviders() == null || tmdb.watchProviders().results() == null
                ? null
                : tmdb.watchProviders().results().get("BR");
        if (br == null) {
            return new WatchInfo(null, List.of());
        }

        // Ordem das ofertas = da melhor para o usuário (já incluída) para a pior (comprar).
        // Uma plataforma que aparece em mais de uma lista fica só com a primeira oferta.
        Map<String, List<TmdbProvider>> offers = new LinkedHashMap<>();
        offers.put("flatrate", br.flatrate());
        offers.put("free", br.free());
        offers.put("ads", br.ads());
        offers.put("rent", br.rent());
        offers.put("buy", br.buy());

        List<WatchProvider> providers = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        offers.forEach((offer, list) -> {
            if (list == null) {
                return;
            }
            for (TmdbProvider p : list) {
                if (seen.add(p.providerId())) {
                    providers.add(new WatchProvider(p.providerId(), p.providerName(),
                            imageUrl(LOGO_BASE_URL, p.logoPath()), offer,
                            watchUrlBuilder.build(p.providerId(), title, br.link())));
                }
            }
        });
        return new WatchInfo(br.link(), providers);
    }
}
