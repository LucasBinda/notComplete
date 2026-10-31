package br.com.watchmatch.recommendation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.client.ResourceAccessException;

import br.com.watchmatch.recommendation.model.AppliedCriteria;
import br.com.watchmatch.recommendation.model.Criteria;
import br.com.watchmatch.recommendation.model.Recommendation;
import br.com.watchmatch.recommendation.model.RecommendResponse;
import br.com.watchmatch.recommendation.model.WatchInfo;
import br.com.watchmatch.recommendation.service.RecommendService;

/**
 * POST /recommend: valida o pedido e devolve o JSON no formato do contrato.
 */
@WebMvcTest(RecommendController.class)
class RecommendControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private RecommendService service;

    @Test
    void semPlataformasDevolve400() {
        var resposta = mvc.post().uri("/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\": \"comédia\", \"providers\": []}")
                .exchange();

        assertThat(resposta).hasStatus(400);
        assertThat(resposta).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void criteriosSaemNoMesmoNivelESemCamposNull() {
        var criterios = new AppliedCriteria(
                new Criteria(null, List.of(35), null, null, 120, null, null, null, null, null),
                List.of(8), null, "Comédia curta", Map.of("genres", "ai", "runtimeMax", "filter"));
        var item = new Recommendation(1, "movie", "Um", "One", 2020, "Sinopse", null, null, List.of(), 7.0, 100,
                null, "12", new WatchInfo(null, List.of()), null);
        when(service.recommend(any(), any()))
                .thenReturn(new RecommendResponse("req-1", 1, false, criterios, List.of(item), false, List.of()));

        var resposta = mvc.post().uri("/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-Id", "req-1")
                .content("{\"query\": \"comédia curta\", \"providers\": [8]}")
                .exchange();

        assertThat(resposta).hasStatusOk();
        assertThat(resposta).hasHeader("X-Request-Id", "req-1");
        assertThat(resposta).bodyJson().extractingPath("$.criteria.genres[0]").isEqualTo(35);
        assertThat(resposta).bodyJson().extractingPath("$.criteria.runtimeMax").isEqualTo(120);
        assertThat(resposta).bodyJson().extractingPath("$.criteria.sources.genres").isEqualTo("ai");
        assertThat(resposta).bodyJson().extractingPath("$.items[0].mediaType").isEqualTo("movie");
        assertThat(resposta).bodyJson().doesNotHavePath("$.items[0].reason");
        assertThat(resposta).bodyJson().doesNotHavePath("$.criteria.mediaType");
    }

    @Test
    void catalogoForaDoArDevolve502() {
        when(service.recommend(any(), any())).thenThrow(new ResourceAccessException("Connection refused"));

        var resposta = mvc.post().uri("/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"providers\": [8]}")
                .exchange();

        assertThat(resposta).hasStatus(502);
        assertThat(resposta).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }
}
