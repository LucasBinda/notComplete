package br.com.watchmatch.catalog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.client.RestClientException;

import br.com.watchmatch.catalog.domain.DiscoverRequest;
import br.com.watchmatch.catalog.domain.Genre;
import br.com.watchmatch.catalog.domain.SortBy;
import br.com.watchmatch.catalog.domain.TitleList;
import br.com.watchmatch.catalog.domain.TitleSummary;
import br.com.watchmatch.catalog.domain.TitleType;
import br.com.watchmatch.catalog.service.DiscoverService;

/**
 * POST /discover: o JSON do contrato entra, vira DiscoverRequest, e o TitleList sai como JSON.
 * O DiscoverService é falso; aqui só importa a "porta de entrada" HTTP.
 */
@WebMvcTest(DiscoverController.class)
class DiscoverControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private DiscoverService service;

    @Test
    void pedidoDoContratoViraDiscoverRequest() {
        when(service.discover(any(), anyInt())).thenReturn(new TitleList(List.of(), false));

        mvc.post().uri("/discover")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"providers": [8, 119], "mediaType": "movie", "genres": [35],
                     "runtimeMax": 120, "sortBy": "rating", "page": 2}
                    """)
                .exchange();

        var pedido = ArgumentCaptor.forClass(DiscoverRequest.class);
        var limit = ArgumentCaptor.forClass(Integer.class);
        verify(service).discover(pedido.capture(), limit.capture());

        assertThat(pedido.getValue().providers()).containsExactly(8, 119);
        assertThat(pedido.getValue().mediaType()).isEqualTo(TitleType.MOVIE);
        assertThat(pedido.getValue().criteria().genres()).containsExactly(35);
        assertThat(pedido.getValue().criteria().runtimeMax()).isEqualTo(120);
        assertThat(pedido.getValue().sortBy()).isEqualTo(SortBy.RATING);
        assertThat(pedido.getValue().page()).isEqualTo(2);
        assertThat(limit.getValue()).isEqualTo(40); // padrão do contrato
    }

    @Test
    void respostaSaiNoFormatoDoContrato() {
        var interestelar = new TitleSummary(157336, TitleType.MOVIE, "Interestelar", "Interstellar", 2014,
                "Uma equipe...", "https://image.tmdb.org/t/p/w500/abc.jpg",
                List.of(new Genre(18, "Drama")), 8.4, "en");
        when(service.discover(any(), anyInt())).thenReturn(new TitleList(List.of(interestelar), true));

        var resposta = mvc.post().uri("/discover")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"providers\": [8]}")
                .exchange();

        assertThat(resposta).hasStatusOk();
        assertThat(resposta).bodyJson().extractingPath("$.items[0].mediaType").isEqualTo("movie");
        assertThat(resposta).bodyJson().extractingPath("$.items[0].title").isEqualTo("Interestelar");
        assertThat(resposta).bodyJson().extractingPath("$.items[0].genres[0].name").isEqualTo("Drama");
        assertThat(resposta).bodyJson().extractingPath("$.hasMore").isEqualTo(true);
    }

    @Test
    void semPlataformasDevolve400() {
        var resposta = mvc.post().uri("/discover")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"providers\": []}")
                .exchange();

        assertThat(resposta).hasStatus(400);
        assertThat(resposta).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void tmdbForaDoArDevolve502() {
        when(service.discover(any(), anyInt())).thenThrow(new RestClientException("timeout"));

        var resposta = mvc.post().uri("/discover")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"providers\": [8]}")
                .exchange();

        assertThat(resposta).hasStatus(502);
        assertThat(resposta).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }
}
