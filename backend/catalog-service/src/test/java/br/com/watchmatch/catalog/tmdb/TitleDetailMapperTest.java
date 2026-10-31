package br.com.watchmatch.catalog.tmdb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.com.watchmatch.catalog.domain.Genre;
import br.com.watchmatch.catalog.domain.TitleDetail;
import br.com.watchmatch.catalog.domain.TitleType;
import br.com.watchmatch.catalog.domain.WatchProvider;
import br.com.watchmatch.catalog.domain.WatchUrlBuilder;
import br.com.watchmatch.catalog.tmdb.TmdbDetail.Cast;
import br.com.watchmatch.catalog.tmdb.TmdbDetail.ContentRating;
import br.com.watchmatch.catalog.tmdb.TmdbDetail.ContentRatings;
import br.com.watchmatch.catalog.tmdb.TmdbDetail.CountryReleases;
import br.com.watchmatch.catalog.tmdb.TmdbDetail.CountryWatch;
import br.com.watchmatch.catalog.tmdb.TmdbDetail.Credits;
import br.com.watchmatch.catalog.tmdb.TmdbDetail.Release;
import br.com.watchmatch.catalog.tmdb.TmdbDetail.ReleaseDates;
import br.com.watchmatch.catalog.tmdb.TmdbDetail.WatchProviders;

/**
 * Traduz os detalhes do TMDB (com onde assistir, elenco e classificação) para TitleDetail.
 */
class TitleDetailMapperTest {

    private static final String LINK = "https://www.themoviedb.org/movie/1/watch?locale=BR";
    private static final TmdbProvider NETFLIX = new TmdbProvider(8, "Netflix", "/n.png");
    private static final TmdbProvider PRIME = new TmdbProvider(119, "Amazon Prime Video", "/p.png");

    private final TitleMapper mapper = new TitleMapper(new WatchUrlBuilder());

    @Test
    void filmeComDuracaoClassificacaoEFundo() {
        var releases = new ReleaseDates(List.of(
                new CountryReleases("US", List.of(new Release("PG-13"))),
                new CountryReleases("BR", List.of(new Release(""), new Release("12")))));
        var tmdb = filme(watch(new CountryWatch(LINK, List.of(NETFLIX), null, null, null, null)), releases);

        TitleDetail detalhe = mapper.toDetail(tmdb, TitleType.MOVIE);

        assertThat(detalhe.title()).isEqualTo("Interestelar");
        assertThat(detalhe.year()).isEqualTo(2014);
        assertThat(detalhe.runtime()).isEqualTo(169);
        assertThat(detalhe.seasons()).isNull();
        assertThat(detalhe.certification()).isEqualTo("12");
        assertThat(detalhe.backdropUrl()).isEqualTo("https://image.tmdb.org/t/p/w1280/fundo.jpg");
        assertThat(detalhe.genres()).containsExactly(new Genre(878, "Ficção científica"));
        assertThat(detalhe.cast()).containsExactly("Matthew McConaughey", "Anne Hathaway");
    }

    @Test
    void classificacaoForaDaTabelaBrasileiraFicaNull() {
        var releases = new ReleaseDates(List.of(new CountryReleases("BR", List.of(new Release("PG-13")))));

        assertThat(mapper.toDetail(filme(null, releases), TitleType.MOVIE).certification()).isNull();
    }

    @Test
    void ondeAssistirTrazCadaPlataformaUmaVezComOMelhorTipoDeOferta() {
        // Netflix aparece na assinatura e no aluguel: fica só a assinatura, que vem primeiro
        var br = new CountryWatch(LINK, List.of(NETFLIX), null, null, List.of(NETFLIX, PRIME), null);

        TitleDetail detalhe = mapper.toDetail(filme(watch(br), null), TitleType.MOVIE);

        assertThat(detalhe.watch().link()).isEqualTo(LINK);
        assertThat(detalhe.watch().providers()).containsExactly(
                new WatchProvider(8, "Netflix", "https://image.tmdb.org/t/p/w92/n.png", "flatrate",
                        "https://www.netflix.com/search?q=Interestelar"),
                new WatchProvider(119, "Amazon Prime Video", "https://image.tmdb.org/t/p/w92/p.png", "rent",
                        "https://www.primevideo.com/search?phrase=Interestelar"));
    }

    @Test
    void semDadosDoBrasilNaoTemOndeAssistir() {
        var soEua = new WatchProviders(Map.of("US", new CountryWatch("x", List.of(NETFLIX), null, null, null, null)));

        TitleDetail detalhe = mapper.toDetail(filme(soEua, null), TitleType.MOVIE);

        assertThat(detalhe.watch().link()).isNull();
        assertThat(detalhe.watch().providers()).isEmpty();
    }

    @Test
    void serieUsaDuracaoDoEpisodioTemporadasEClassificacaoDaTv() {
        var tmdb = new TmdbDetail(1396, null, "Breaking Bad", null, "Breaking Bad", "Um professor...",
                "/bb.jpg", null, null, "2008-01-20", 8.9, List.of(new Genre(18, "Drama")),
                null, List.of(47, 45), 5, "en", null, null,
                new ContentRatings(List.of(new ContentRating("BR", "16"))), null);

        TitleDetail detalhe = mapper.toDetail(tmdb, TitleType.TV);

        assertThat(detalhe.title()).isEqualTo("Breaking Bad");
        assertThat(detalhe.runtime()).isEqualTo(47);
        assertThat(detalhe.seasons()).isEqualTo(5);
        assertThat(detalhe.certification()).isEqualTo("16");
        assertThat(detalhe.cast()).isEmpty();
    }

    private static WatchProviders watch(CountryWatch br) {
        return new WatchProviders(Map.of("BR", br));
    }

    private static TmdbDetail filme(WatchProviders watch, ReleaseDates releases) {
        return new TmdbDetail(1, "Interestelar", null, "Interstellar", null, "Uma equipe...",
                "/abc.jpg", "/fundo.jpg", "2014-11-05", null, 8.4, List.of(new Genre(878, "Ficção científica")),
                169, null, null, "en", watch, releases, null,
                new Credits(List.of(new Cast("Matthew McConaughey"), new Cast("Anne Hathaway"))));
    }
}
