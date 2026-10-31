package br.com.watchmatch.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.watchmatch.recommendation.model.Criteria;
import br.com.watchmatch.recommendation.model.Llm.IntentResponse;

/**
 * Junta os filtros da tela com o que a IA entendeu do texto. O filtro da tela sempre vence.
 */
class CriteriaMergerTest {

    private final CriteriaMerger merger = new CriteriaMerger();

    @Test
    void semIaUsaSoOsFiltros() {
        var filtros = new Criteria("movie", List.of(35), null, null, 120, null, null, null, null, null);

        var resultado = merger.merge(filtros, null, List.of(8, 119));

        assertThat(resultado.criteria()).isEqualTo(filtros);
        assertThat(resultado.providers()).containsExactly(8, 119);
        assertThat(resultado.sources()).containsEntry("mediaType", "filter")
                .containsEntry("genres", "filter")
                .containsEntry("runtimeMax", "filter")
                .hasSize(3);
    }

    @Test
    void iaPreencheOQueOsFiltrosDeixaramVazio() {
        var filtros = new Criteria(null, null, null, null, 120, null, null, null, null, null);
        var ia = intent(new Criteria("movie", List.of(35, 10749), List.of(27), null, 90, 2010, null, null, null,
                List.of("romantic comedy")), List.of());

        var resultado = merger.merge(filtros, ia, List.of(8));

        assertThat(resultado.criteria().mediaType()).isEqualTo("movie");
        assertThat(resultado.criteria().genres()).containsExactly(35, 10749);
        assertThat(resultado.criteria().excludeGenres()).containsExactly(27);
        assertThat(resultado.criteria().runtimeMax()).isEqualTo(120); // o filtro venceu os 90 da IA
        assertThat(resultado.criteria().yearFrom()).isEqualTo(2010);
        assertThat(resultado.criteria().keywords()).containsExactly("romantic comedy");
        assertThat(resultado.sources())
                .containsEntry("runtimeMax", "filter")
                .containsEntry("genres", "ai")
                .containsEntry("excludeGenres", "ai")
                .containsEntry("yearFrom", "ai");
    }

    @Test
    void generoPedidoNaTelaNuncaFicaProibidoPelaIa() {
        var filtros = new Criteria(null, List.of(27), null, null, null, null, null, null, null, null);
        var ia = intent(new Criteria(null, null, List.of(27, 99), null, null, null, null, null, null, null), List.of());

        var resultado = merger.merge(filtros, ia, List.of(8));

        assertThat(resultado.criteria().excludeGenres()).containsExactly(99);
    }

    @Test
    void plataformaCitadaNoTextoRestringeAsDoUsuario() {
        // "quero algo na Netflix", e o usuário assina Netflix e Prime
        var resultado = merger.merge(Criteria.empty(), intent(Criteria.empty(), List.of(8)), List.of(8, 119));

        assertThat(resultado.providers()).containsExactly(8);
        assertThat(resultado.sources()).containsEntry("providers", "ai");
    }

    @Test
    void plataformaCitadaQueOUsuarioNaoAssinaEIgnorada() {
        var resultado = merger.merge(Criteria.empty(), intent(Criteria.empty(), List.of(337)), List.of(8, 119));

        assertThat(resultado.providers()).containsExactly(8, 119);
        assertThat(resultado.sources()).doesNotContainKey("providers");
    }

    private static IntentResponse intent(Criteria criteria, List<Integer> providers) {
        return new IntentResponse(criteria, providers, null, "resumo", false);
    }
}
