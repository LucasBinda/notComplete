package br.com.watchmatch.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import br.com.watchmatch.catalog.domain.TitleDetail;
import br.com.watchmatch.catalog.domain.TitleRef;
import br.com.watchmatch.catalog.domain.TitleType;
import br.com.watchmatch.catalog.domain.WatchUrlBuilder;
import br.com.watchmatch.catalog.tmdb.TitleMapper;
import br.com.watchmatch.catalog.tmdb.TmdbClient;
import br.com.watchmatch.catalog.tmdb.TmdbDetail;

/**
 * Busca os detalhes de um ou vários títulos (o /titles/lookup do recommendation-service).
 */
class TitleServiceTest {

    private final TmdbClient client = mock(TmdbClient.class);
    private final TitleService service = new TitleService(client, new TitleMapper(new WatchUrlBuilder()));

    @Test
    void lookupMantemAOrdemDoPedidoEOmiteOsInexistentes() {
        when(client.details(TitleType.MOVIE, 1)).thenReturn(detalhe(1, "Filme"));
        when(client.details(TitleType.TV, 2)).thenReturn(detalhe(2, "Série"));
        when(client.details(TitleType.MOVIE, 999)).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        List<TitleDetail> itens = service.lookup(List.of(
                new TitleRef(TitleType.TV, 2),
                new TitleRef(TitleType.MOVIE, 999),
                new TitleRef(TitleType.MOVIE, 1)));

        assertThat(itens).extracting(TitleDetail::id).containsExactly(2, 1);
    }

    private static TmdbDetail detalhe(int id, String nome) {
        return new TmdbDetail(id, nome, nome, nome, nome, "", null, null, "2020-01-01", "2020-01-01",
                7.0, List.of(), 100, List.of(), 1, "en", null, null, null, null);
    }
}
