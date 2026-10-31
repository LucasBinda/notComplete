package br.com.watchmatch.catalog.tmdb;

import java.util.List;
import java.util.Map;

import br.com.watchmatch.catalog.domain.Genre;
import br.com.watchmatch.catalog.domain.TitleSummary;
import br.com.watchmatch.catalog.domain.TitleType;

public class TitleMapper {

    // w500 = pôster com 500px de largura, bom para os cards
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";

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
                posterUrlOf(tmdb.posterPath()),
                genresOf(tmdb.genreIds(), genreNames),
                tmdb.voteAverage(),
                tmdb.originalLanguage());
    }

    // "2014-11-05" vira 2014; null ou "" viram null
    private Integer yearOf(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return Integer.valueOf(date.substring(0, 4));
    }

    private String posterUrlOf(String posterPath) {
        return posterPath == null ? null : POSTER_BASE_URL + posterPath;
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
}
