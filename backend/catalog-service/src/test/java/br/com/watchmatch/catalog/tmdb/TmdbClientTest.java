package br.com.watchmatch.catalog.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import br.com.watchmatch.catalog.domain.TitleType;

/**
 * Faz as chamadas HTTP para a API do TMDB.
 * Nos testes, um TMDB falso (MockRestServiceServer) responde no lugar do real.
 */
class TmdbClientTest {

    // A ordem importa: o TMDB falso se liga no builder ANTES do TmdbClient usar o builder
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer tmdbFalso = MockRestServiceServer.bindTo(builder).build();
    private final TmdbClient client = new TmdbClient(builder, "chave-de-teste");

    @Test
    void discoverMandaOsParametrosEAChave() {
        tmdbFalso.expect(requestTo("https://api.themoviedb.org/3/discover/movie?watch_region=BR"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer chave-de-teste"))
                .andRespond(withSuccess("{\"page\":1,\"results\":[]}", MediaType.APPLICATION_JSON));

        client.discover(TitleType.MOVIE, Map.of("watch_region", "BR"));

        tmdbFalso.verify();
    }

    @Test
    void discoverTransformaARespostaEmTmdbPage() {
        tmdbFalso.expect(requestTo("https://api.themoviedb.org/3/discover/movie?watch_region=BR"))
                .andRespond(withSuccess("""
                    {"page": 1, "total_pages": 40, "results": [{
                        "id": 157336,
                        "title": "Interestelar",
                        "original_title": "Interstellar",
                        "overview": "Uma equipe de exploradores...",
                        "poster_path": "/abc.jpg",
                        "release_date": "2014-11-05",
                        "vote_average": 8.4,
                        "genre_ids": [12, 18, 878],
                        "original_language": "en",
                        "popularity": 99.9
                    }]}
                    """, MediaType.APPLICATION_JSON));

        TmdbPage page = client.discover(TitleType.MOVIE, Map.of("watch_region", "BR"));

        assertThat(page.totalPages()).isEqualTo(40);
        TmdbResult interestelar = page.results().get(0);
        assertThat(interestelar.id()).isEqualTo(157336);
        assertThat(interestelar.title()).isEqualTo("Interestelar");
        assertThat(interestelar.posterPath()).isEqualTo("/abc.jpg");
        assertThat(interestelar.releaseDate()).isEqualTo("2014-11-05");
        assertThat(interestelar.genreIds()).containsExactly(12, 18, 878);
    }
}
