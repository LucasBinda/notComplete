package br.com.watchmatch.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.watchmatch.catalog.domain.Criteria;
import br.com.watchmatch.catalog.domain.DiscoverQueryBuilder;
import br.com.watchmatch.catalog.domain.DiscoverRequest;
import br.com.watchmatch.catalog.domain.Genre;
import br.com.watchmatch.catalog.domain.SortBy;
import br.com.watchmatch.catalog.domain.TitleList;
import br.com.watchmatch.catalog.domain.TitleSummary;
import br.com.watchmatch.catalog.domain.TitleType;
import br.com.watchmatch.catalog.tmdb.TitleMapper;
import br.com.watchmatch.catalog.tmdb.TmdbClient;
import br.com.watchmatch.catalog.tmdb.TmdbPage;
import br.com.watchmatch.catalog.tmdb.TmdbResult;

/**
 * Junta as peças: monta os parâmetros, chama o TMDB e traduz a resposta.
 * O TmdbClient é falso (Mockito); o builder e o mapper são os de verdade.
 */
class DiscoverServiceTest {

    private final TmdbClient client = mock(TmdbClient.class);
    private final DiscoverService service = new DiscoverService(client, new DiscoverQueryBuilder(), new TitleMapper());

    @BeforeEach
    void generosDoTmdb() {
        when(client.genres(TitleType.MOVIE)).thenReturn(Map.of(18, "Drama"));
        when(client.genres(TitleType.TV)).thenReturn(Map.of(18, "Drama"));
    }

    @Test
    void comMediaTypeBuscaSoAqueleTipo() {
        when(client.discover(eq(TitleType.MOVIE), anyMap()))
                .thenReturn(new TmdbPage(1, 1, List.of(filme(1, "Interestelar"))));

        TitleList lista = service.discover(pedido(TitleType.MOVIE, 1), 40);

        assertThat(lista.items()).extracting(TitleSummary::title).containsExactly("Interestelar");
        assertThat(lista.items().get(0).genres()).containsExactly(new Genre(18, "Drama"));
        verify(client, never()).discover(eq(TitleType.TV), anyMap());
    }

    @Test
    void semMediaTypeIntercalaFilmesESeries() {
        when(client.discover(eq(TitleType.MOVIE), anyMap()))
                .thenReturn(new TmdbPage(1, 1, List.of(filme(1, "F1"), filme(2, "F2"), filme(3, "F3"))));
        when(client.discover(eq(TitleType.TV), anyMap()))
                .thenReturn(new TmdbPage(1, 1, List.of(serie(4, "S1"))));

        TitleList lista = service.discover(pedido(null, 1), 40);

        assertThat(lista.items()).extracting(TitleSummary::title).containsExactly("F1", "S1", "F2", "F3");
    }

    @Test
    void limitCortaALista() {
        when(client.discover(eq(TitleType.MOVIE), anyMap()))
                .thenReturn(new TmdbPage(1, 1, List.of(filme(1, "F1"), filme(2, "F2"), filme(3, "F3"))));

        TitleList lista = service.discover(pedido(TitleType.MOVIE, 1), 2);

        assertThat(lista.items()).extracting(TitleSummary::title).containsExactly("F1", "F2");
    }

    @Test
    void hasMoreQuandoOTmdbTemMaisPaginas() {
        when(client.discover(eq(TitleType.MOVIE), anyMap()))
                .thenReturn(new TmdbPage(2, 5, List.of(filme(1, "F1"))));

        assertThat(service.discover(pedido(TitleType.MOVIE, 2), 40).hasMore()).isTrue();
    }

    @Test
    void semHasMoreNaUltimaPagina() {
        when(client.discover(eq(TitleType.MOVIE), anyMap()))
                .thenReturn(new TmdbPage(5, 5, List.of(filme(1, "F1"))));

        assertThat(service.discover(pedido(TitleType.MOVIE, 5), 40).hasMore()).isFalse();
    }

    @Test
    void generosSaoBuscadosNoTmdbUmaVezSo() {
        when(client.discover(eq(TitleType.MOVIE), anyMap()))
                .thenReturn(new TmdbPage(1, 1, List.of(filme(1, "F1"))));

        service.discover(pedido(TitleType.MOVIE, 1), 40);
        service.discover(pedido(TitleType.MOVIE, 1), 40);

        verify(client, times(1)).genres(TitleType.MOVIE);
    }

    @Test
    void respostaSemResultadosViraListaVazia() {
        when(client.discover(eq(TitleType.MOVIE), anyMap())).thenReturn(new TmdbPage(1, 0, null));

        TitleList lista = service.discover(pedido(TitleType.MOVIE, 1), 40);

        assertThat(lista.items()).isEmpty();
        assertThat(lista.hasMore()).isFalse();
    }

    private static DiscoverRequest pedido(TitleType mediaType, int page) {
        return new DiscoverRequest(Criteria.empty(), List.of(8), SortBy.POPULARITY, mediaType, page);
    }

    private static TmdbResult filme(int id, String titulo) {
        return new TmdbResult(id, titulo, null, titulo, null, "", null, "2020-01-01", null, 7.0, List.of(18), "en");
    }

    private static TmdbResult serie(int id, String nome) {
        return new TmdbResult(id, null, nome, null, nome, "", null, null, "2020-01-01", 7.0, List.of(18), "en");
    }
}
