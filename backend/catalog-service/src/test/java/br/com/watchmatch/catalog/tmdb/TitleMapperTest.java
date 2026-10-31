package br.com.watchmatch.catalog.tmdb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.com.watchmatch.catalog.domain.Genre;
import br.com.watchmatch.catalog.domain.TitleSummary;
import br.com.watchmatch.catalog.domain.TitleType;

/**
 * Traduz um resultado do TMDB (TmdbResult) para o formato do nosso contrato (TitleSummary).
 */
class TitleMapperTest {

    private final TitleMapper mapper = new TitleMapper();
    private final Map<Integer, String> generos = Map.of(18, "Drama", 878, "Ficção científica");

    @Test
    void filmeUsaTitleEReleaseDate() {
        var tmdb = new TmdbResult(157336, "Interestelar", null, "Interstellar", null,
                "Uma equipe...", "/abc.jpg", "2014-11-05", null, 8.4, List.of(18), "en");

        TitleSummary titulo = mapper.toSummary(tmdb, TitleType.MOVIE, generos);

        assertThat(titulo.id()).isEqualTo(157336);
        assertThat(titulo.mediaType()).isEqualTo(TitleType.MOVIE);
        assertThat(titulo.title()).isEqualTo("Interestelar");
        assertThat(titulo.originalTitle()).isEqualTo("Interstellar");
        assertThat(titulo.year()).isEqualTo(2014);
    }

    @Test
    void serieUsaNameEFirstAirDate() {
        var tmdb = new TmdbResult(1396, null, "Breaking Bad", null, "Breaking Bad",
                "", null, null, "2008-01-20", 8.9, List.of(), "en");

        TitleSummary titulo = mapper.toSummary(tmdb, TitleType.TV, generos);

        assertThat(titulo.title()).isEqualTo("Breaking Bad");
        assertThat(titulo.year()).isEqualTo(2008);
    }

    @Test
    void posterViraUrlCompleta() {
        var tmdb = new TmdbResult(1, "X", null, "X", null,
                "", "/abc.jpg", "2020-01-01", null, 7.0, List.of(), "en");

        assertThat(mapper.toSummary(tmdb, TitleType.MOVIE, generos).posterUrl())
                .isEqualTo("https://image.tmdb.org/t/p/w500/abc.jpg");
    }

    @Test
    void semPosterESemDataFicamNull() {
        // O TMDB às vezes manda a data como "" em vez de null
        var tmdb = new TmdbResult(1, "X", null, "X", null,
                "", null, "", null, 7.0, List.of(), "en");

        TitleSummary titulo = mapper.toSummary(tmdb, TitleType.MOVIE, generos);

        assertThat(titulo.posterUrl()).isNull();
        assertThat(titulo.year()).isNull();
    }

    @Test
    void generosViramIdENomeEIdsDesconhecidosSaoIgnorados() {
        var tmdb = new TmdbResult(1, "X", null, "X", null,
                "", null, "2020-01-01", null, 7.0, List.of(18, 878, 99999), "en");

        assertThat(mapper.toSummary(tmdb, TitleType.MOVIE, generos).genres())
                .containsExactly(new Genre(18, "Drama"), new Genre(878, "Ficção científica"));
    }

    @Test
    void semListaDeGenerosViraListaVazia() {
        var tmdb = new TmdbResult(1, "X", null, "X", null,
                "", null, "2020-01-01", null, 7.0, null, "en");

        assertThat(mapper.toSummary(tmdb, TitleType.MOVIE, generos).genres()).isEmpty();
    }
}
