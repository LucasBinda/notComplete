package br.com.watchmatch.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Monta o link do botão "Abrir na plataforma". O TMDB não dá o endereço do título
 * dentro da plataforma, então usamos a busca da própria plataforma com o nome do título.
 */
class WatchUrlBuilderTest {

    private static final String LINK_TMDB = "https://www.themoviedb.org/movie/157336/watch?locale=BR";

    private final WatchUrlBuilder builder = new WatchUrlBuilder();

    @Test
    void netflixUsaABuscaDaNetflix() {
        assertThat(builder.build(8, "Interestelar", LINK_TMDB))
                .isEqualTo("https://www.netflix.com/search?q=Interestelar");
    }

    @Test
    void primeVideoUsaABuscaDoPrime() {
        assertThat(builder.build(119, "Interestelar", LINK_TMDB))
                .isEqualTo("https://www.primevideo.com/search?phrase=Interestelar");
    }

    @Test
    void acentosEEspacosSaoCodificados() {
        assertThat(builder.build(8, "Sobrevivência total", LINK_TMDB))
                .isEqualTo("https://www.netflix.com/search?q=Sobreviv%C3%AAncia%20total");
    }

    @Test
    void plataformaSemBuscaConhecidaUsaOLinkDoTmdb() {
        assertThat(builder.build(337, "Interestelar", LINK_TMDB)).isEqualTo(LINK_TMDB);
    }
}
