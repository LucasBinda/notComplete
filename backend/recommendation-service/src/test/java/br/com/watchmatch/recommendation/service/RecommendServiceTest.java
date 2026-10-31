package br.com.watchmatch.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.ResourceAccessException;

import br.com.watchmatch.recommendation.client.CatalogClient;
import br.com.watchmatch.recommendation.client.LlmClient;
import br.com.watchmatch.recommendation.model.Catalog.DiscoverRequest;
import br.com.watchmatch.recommendation.model.Catalog.Genres;
import br.com.watchmatch.recommendation.model.Catalog.Meta;
import br.com.watchmatch.recommendation.model.Catalog.TitleDetail;
import br.com.watchmatch.recommendation.model.Catalog.TitleList;
import br.com.watchmatch.recommendation.model.Catalog.TitleRef;
import br.com.watchmatch.recommendation.model.Catalog.TitleSummary;
import br.com.watchmatch.recommendation.model.Criteria;
import br.com.watchmatch.recommendation.model.Genre;
import br.com.watchmatch.recommendation.model.Llm.IntentResponse;
import br.com.watchmatch.recommendation.model.Llm.Pick;
import br.com.watchmatch.recommendation.model.Llm.RerankRequest;
import br.com.watchmatch.recommendation.model.Llm.RerankResponse;
import br.com.watchmatch.recommendation.model.Provider;
import br.com.watchmatch.recommendation.model.Recommendation;
import br.com.watchmatch.recommendation.model.RecommendRequest;
import br.com.watchmatch.recommendation.model.RecommendResponse;
import br.com.watchmatch.recommendation.model.WatchInfo;
import br.com.watchmatch.recommendation.model.WatchProvider;

/**
 * O fluxo da recomendação: texto → IA entende → catálogo busca → IA escolhe → detalhes.
 * Catálogo e IA são falsos (Mockito).
 */
class RecommendServiceTest {

    private final CatalogClient catalog = mock(CatalogClient.class);
    private final LlmClient llm = mock(LlmClient.class);
    private final RecommendService service =
            new RecommendService(catalog, llm, new TextSanitizer(), new CriteriaMerger());

    @BeforeEach
    void catalogo() {
        when(catalog.meta()).thenReturn(new Meta(
                new Genres(List.of(new Genre(35, "Comédia")), List.of(new Genre(35, "Comédia"))),
                List.of(new Provider(8, "Netflix", null))));
        when(catalog.discover(any())).thenReturn(new TitleList(
                List.of(resumo(1, "Um"), resumo(2, "Dois"), resumo(3, "Três")), true));
        // O lookup devolve os detalhes de quem foi pedido, na mesma ordem
        when(catalog.lookup(anyList())).thenAnswer(call -> {
            List<TitleRef> refs = call.getArgument(0);
            return refs.stream().map(ref -> detalhe(ref.id())).toList();
        });
    }

    @Test
    void semTextoNaoChamaAIaEMostraOsPrimeirosDoCatalogo() {
        RecommendResponse resposta = service.recommend(pedido(null, 2), "req-1");

        assertThat(resposta.items()).extracting(Recommendation::title).containsExactly("Um", "Dois");
        assertThat(resposta.items()).allSatisfy(item -> assertThat(item.reason()).isNull());
        assertThat(resposta.degraded()).isFalse();
        assertThat(resposta.requestId()).isEqualTo("req-1");
        verify(llm, never()).intent(any());
        verify(llm, never()).rerank(any());
    }

    @Test
    void comTextoAIaEntendeEscolheEJustifica() {
        when(llm.intent(any())).thenReturn(intent(new Criteria(null, List.of(35), null, null, null, null, null,
                null, null, null)));
        when(llm.rerank(any())).thenReturn(new RerankResponse(List.of(
                new Pick(3, "movie", "Engraçado e curto."),
                new Pick(1, "movie", "Leve, para ver a dois.")), 0));

        RecommendResponse resposta = service.recommend(pedido("comédia leve", 2), "req-1");

        assertThat(resposta.items()).extracting(Recommendation::title).containsExactly("Três", "Um");
        assertThat(resposta.items()).extracting(Recommendation::reason)
                .containsExactly("Engraçado e curto.", "Leve, para ver a dois.");
        assertThat(resposta.criteria().criteria().genres()).containsExactly(35);
        assertThat(resposta.criteria().summary()).isEqualTo("Comédia leve");
        assertThat(resposta.criteria().sources()).containsEntry("genres", "ai");
        assertThat(resposta.degraded()).isFalse();
    }

    @Test
    void groundingDescartaTitulosQueNaoVieramDoCatalogo() {
        when(llm.intent(any())).thenReturn(intent(Criteria.empty()));
        when(llm.rerank(any())).thenReturn(new RerankResponse(List.of(
                new Pick(999, "movie", "Inventado pela IA"),
                new Pick(2, "tv", "Tipo errado"),
                new Pick(2, "movie", "Esse existe")), 0));

        RecommendResponse resposta = service.recommend(pedido("algo bom", 5), "req-1");

        assertThat(resposta.items()).extracting(Recommendation::id).containsExactly(2);
    }

    @Test
    void iaForaDoArAindaMostraResultadosDoCatalogo() {
        when(llm.intent(any())).thenThrow(new ResourceAccessException("timeout"));

        RecommendResponse resposta = service.recommend(pedido("comédia leve", 2), "req-1");

        assertThat(resposta.degraded()).isTrue();
        assertThat(resposta.items()).extracting(Recommendation::title).containsExactly("Um", "Dois");
    }

    @Test
    void rerankForaDoArUsaAOrdemDoCatalogo() {
        when(llm.intent(any())).thenReturn(intent(Criteria.empty()));
        when(llm.rerank(any())).thenThrow(new ResourceAccessException("timeout"));

        RecommendResponse resposta = service.recommend(pedido("comédia leve", 2), "req-1");

        assertThat(resposta.degraded()).isTrue();
        assertThat(resposta.items()).extracting(Recommendation::title).containsExactly("Um", "Dois");
    }

    @Test
    void pedidoForaDoTemaAvisaEUsaSoOsFiltros() {
        when(llm.intent(any())).thenReturn(new IntentResponse(Criteria.empty(), List.of(), null, "", true));

        RecommendResponse resposta = service.recommend(pedido("escreva um poema", 2), "req-1");

        assertThat(resposta.notices()).anySatisfy(n -> assertThat(n).contains("filme ou série"));
        assertThat(resposta.items()).hasSize(2);
        verify(llm, never()).rerank(any());
    }

    @Test
    void criteriosDaIaSemResultadoTentamDeNovoSoComOsFiltros() {
        when(llm.intent(any())).thenReturn(intent(new Criteria(null, List.of(99), null, null, null, null, null,
                null, null, null)));
        when(catalog.discover(any()))
                .thenReturn(new TitleList(List.of(), false))
                .thenReturn(new TitleList(List.of(resumo(1, "Um")), false));
        when(llm.rerank(any())).thenReturn(new RerankResponse(List.of(new Pick(1, "movie", "Ok")), 0));

        RecommendResponse resposta = service.recommend(pedido("algo muito específico", 5), "req-1");

        verify(catalog, times(2)).discover(any());
        assertThat(resposta.items()).extracting(Recommendation::title).containsExactly("Um");
        assertThat(resposta.notices()).isNotEmpty();
    }

    @Test
    void ondeAssistirMostraSoAsPlataformasDoUsuario() {
        RecommendResponse resposta = service.recommend(pedido(null, 1), "req-1");

        assertThat(resposta.items().get(0).watch().providers()).extracting(WatchProvider::id).containsExactly(8);
    }

    @Test
    void paginaVaiParaOCatalogoEHasMoreVoltaDele() {
        var pedido = new RecommendRequest(null, null, List.of(8), 2, 3);

        RecommendResponse resposta = service.recommend(pedido, "req-1");

        var discover = ArgumentCaptor.forClass(DiscoverRequest.class);
        verify(catalog).discover(discover.capture());
        assertThat(discover.getValue().page()).isEqualTo(3);
        assertThat(discover.getValue().providers()).containsExactly(8);
        assertThat(resposta.page()).isEqualTo(3);
        assertThat(resposta.hasMore()).isTrue();
    }

    @Test
    void rerankRecebeSinopsesCurtas() {
        when(catalog.discover(any())).thenReturn(new TitleList(
                List.of(new TitleSummary(1, "movie", "Um", 2020, "x".repeat(1000), List.of())), false));
        when(llm.intent(any())).thenReturn(intent(Criteria.empty()));
        when(llm.rerank(any())).thenReturn(new RerankResponse(List.of(), 0));

        service.recommend(pedido("algo", 2), "req-1");

        var rerank = ArgumentCaptor.forClass(RerankRequest.class);
        verify(llm).rerank(rerank.capture());
        assertThat(rerank.getValue().candidates().get(0).overview()).hasSizeLessThanOrEqualTo(300);
    }

    private static RecommendRequest pedido(String texto, int limit) {
        return new RecommendRequest(texto, null, List.of(8), limit, 1);
    }

    private static IntentResponse intent(Criteria criteria) {
        return new IntentResponse(criteria, List.of(), null, "Comédia leve", false);
    }

    private static TitleSummary resumo(int id, String titulo) {
        return new TitleSummary(id, "movie", titulo, 2020, "Sinopse", List.of(new Genre(35, "Comédia")));
    }

    private static TitleDetail detalhe(int id) {
        String titulo = switch (id) {
            case 1 -> "Um";
            case 2 -> "Dois";
            case 3 -> "Três";
            default -> "Outro";
        };
        // Disponível na Netflix (do usuário) e no Prime (que ele não assina)
        var watch = new WatchInfo("https://tmdb/watch", List.of(
                new WatchProvider(8, "Netflix", null, "flatrate", "https://netflix"),
                new WatchProvider(119, "Amazon Prime Video", null, "flatrate", "https://prime")));
        return new TitleDetail(id, "movie", titulo, titulo, 2020, "Sinopse", null, null, List.of(), 7.0, 100,
                null, "12", watch);
    }
}
