package br.com.watchmatch.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Traduz os critérios do WatchMatch para os parâmetros de
 * GET /discover/movie e /discover/tv do TMDB.
 */
class DiscoverQueryBuilderTest {

    private final DiscoverQueryBuilder builder = new DiscoverQueryBuilder();

    @Test
    void plataformasViramFiltroDeStreamingNoBrasil() {
        var request = new DiscoverRequest(Criteria.empty(), List.of(8, 119));

        Map<String, String> params = builder.build(request, TitleType.MOVIE);

        assertThat(params)
                .containsEntry("watch_region", "BR")
                .containsEntry("with_watch_providers", "8|119");
    }
    @Test
    void sempreEnviaOsParametrosPadrao() {
        var request = new DiscoverRequest(Criteria.empty(), List.of(8));

        Map<String, String> params = builder.build(request, TitleType.MOVIE);

        assertThat(params)
                .containsEntry("with_watch_monetization_types", "flatrate|free|ads")
                .containsEntry("language", "pt-BR")
                .containsEntry("vote_count.gte", "50");
    }
    @Test
    void generosDesejadosEProibidos() {
        var criteria = Criteria.empty()
                .withGenres(List.of(35, 10749))
                .withExcludeGenres(List.of(27));
        
        var request = new DiscoverRequest(criteria, List.of(8));

        Map<String, String> params = builder.build(request, TitleType.MOVIE);

        assertThat(params)
                .containsEntry("with_genres", "35|10749")
                .containsEntry("without_genres", "27");
    }

    @Test
    void duracaoMaximaEmMinutos() {
        var criteria = Criteria.empty().withRuntimeMax(120);
        var request = new DiscoverRequest(criteria, List.of(8));

        Map<String, String> params = builder.build(request, TitleType.MOVIE);

        assertThat(params).containsEntry("with_runtime.lte", "120");
    }

    @Test
    void intervaloDeAnosParaFilmeUsaDataDeLancamento() {
        var criteria = Criteria.empty().withYearFrom(2000).withYearTo(2009);
        var request = new DiscoverRequest(criteria, List.of(8));

        Map<String, String> params = builder.build(request, TitleType.MOVIE);

        assertThat(params)
                .containsEntry("primary_release_date.gte", "2000-01-01")
                .containsEntry("primary_release_date.lte", "2009-12-31");
    }

    @Test
    void intervaloDeAnosParaSerieUsaDataDaPrimeiraExibicao() {
        var criteria = Criteria.empty().withYearFrom(2000).withYearTo(2009);
        var request = new DiscoverRequest(criteria, List.of(8));

        Map<String, String> params = builder.build(request, TitleType.TV);

        assertThat(params)
                .containsEntry("first_air_date.gte", "2000-01-01")
                .containsEntry("first_air_date.lte", "2009-12-31")
                .doesNotContainKey("primary_release_date.gte");
    }

    @Test
    void classificacaoIndicativaMaximaParaFilme() {
        var criteria = Criteria.empty().withCertificationMax("14");
        var request = new DiscoverRequest(criteria, List.of(8));

        Map<String, String> params = builder.build(request, TitleType.MOVIE);

        assertThat(params)
                .containsEntry("certification_country", "BR")
                .containsEntry("certification.lte", "14");
    }

    @Test
    void classificacaoIndicativaNaoEEnviadaParaSerie() {
        // O /discover/tv do TMDB não aceita filtro de classificação
        var criteria = Criteria.empty().withCertificationMax("14");
        var request = new DiscoverRequest(criteria, List.of(8));

        Map<String, String> params = builder.build(request, TitleType.TV);

        assertThat(params)
                .doesNotContainKey("certification_country")
                .doesNotContainKey("certification.lte");
    }

    @Test
    void ordenacaoPadraoEPorPopularidade() {
        var request = new DiscoverRequest(Criteria.empty(), List.of(8));

        Map<String, String> params = builder.build(request, TitleType.MOVIE);

        assertThat(params).containsEntry("sort_by", "popularity.desc");
    }

    @Test
    void ordenacaoPorNota() {
        var request = new DiscoverRequest(Criteria.empty(), List.of(8), SortBy.RATING);

        Map<String, String> params = builder.build(request, TitleType.MOVIE);

        assertThat(params).containsEntry("sort_by", "vote_average.desc");
    }

    @Test
    void ordenacaoPorMaisRecenteUsaOCampoDeDataDoTipo() {
        var request = new DiscoverRequest(Criteria.empty(), List.of(8), SortBy.RECENT);

        assertThat(builder.build(request, TitleType.MOVIE))
                .containsEntry("sort_by", "primary_release_date.desc");
        assertThat(builder.build(request, TitleType.TV))
                .containsEntry("sort_by", "first_air_date.desc");
    }

    @Test
    void criteriosVaziosGeramSoOsParametrosPadrao() {
        var request = new DiscoverRequest(Criteria.empty(), List.of(8), null);

        Map<String, String> params = builder.build(request, TitleType.MOVIE);

        assertThat(params)
                .containsOnlyKeys(
                        "watch_region",
                        "with_watch_providers",
                        "with_watch_monetization_types",
                        "language",
                        "vote_count.gte",
                        "sort_by",
                        "page")
                .doesNotContainValue(null);
    }

    @Test
    void listasDeGenerosNullSaoTratadasComoVazias() {
        // Quando o JSON do pedido não traz "genres", o Spring preenche o espaço com null
        var criteria = Criteria.empty().withGenres(null).withExcludeGenres(null);
        var request = new DiscoverRequest(criteria, List.of(8));

        Map<String, String> params = builder.build(request, TitleType.MOVIE);

        assertThat(params)
                .doesNotContainKey("with_genres")
                .doesNotContainKey("without_genres");
    }

    @Test
    void paginaPadraoEAPrimeira() {
        var request = new DiscoverRequest(Criteria.empty(), List.of(8));

        assertThat(builder.build(request, TitleType.MOVIE)).containsEntry("page", "1");
    }

    @Test
    void paginaPedidaVaiParaOTmdb() {
        var request = new DiscoverRequest(Criteria.empty(), List.of(8), SortBy.POPULARITY, null, 3);

        assertThat(builder.build(request, TitleType.MOVIE)).containsEntry("page", "3");
    }
}
