package br.com.watchmatch.catalog.tmdb;

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
}
