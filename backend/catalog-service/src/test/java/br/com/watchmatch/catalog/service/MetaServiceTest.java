package br.com.watchmatch.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.watchmatch.catalog.domain.Genre;
import br.com.watchmatch.catalog.domain.Meta;
import br.com.watchmatch.catalog.domain.Provider;
import br.com.watchmatch.catalog.domain.TitleType;
import br.com.watchmatch.catalog.tmdb.TmdbClient;
import br.com.watchmatch.catalog.tmdb.TmdbProvider;

/**
 * Monta o /meta: gêneros de filme e série + as plataformas que o site oferece.
 */
class MetaServiceTest {

    private final TmdbClient client = mock(TmdbClient.class);
    // Ordem da configuração = ordem de relevância na tela
    private final MetaService service = new MetaService(client, List.of(119, 8));

    @BeforeEach
    void tmdb() {
        when(client.genres(TitleType.MOVIE)).thenReturn(Map.of(35, "Comédia", 28, "Ação", 18, "Drama"));
        when(client.genres(TitleType.TV)).thenReturn(Map.of(18, "Drama"));
        when(client.providers()).thenReturn(List.of(
                new TmdbProvider(8, "Netflix", "/n.png"),
                new TmdbProvider(2, "Apple TV Store", "/a.png"),
                new TmdbProvider(119, "Amazon Prime Video", "/p.png")));
    }

    @Test
    void generosEmOrdemAlfabetica() {
        Meta meta = service.meta();

        assertThat(meta.genres().movie()).extracting(Genre::name).containsExactly("Ação", "Comédia", "Drama");
        assertThat(meta.genres().tv()).containsExactly(new Genre(18, "Drama"));
    }

    @Test
    void soAsPlataformasConfiguradasNaOrdemDaConfiguracao() {
        assertThat(service.meta().providers()).containsExactly(
                new Provider(119, "Amazon Prime Video", "https://image.tmdb.org/t/p/w92/p.png"),
                new Provider(8, "Netflix", "https://image.tmdb.org/t/p/w92/n.png"));
    }

    @Test
    void buscaNoTmdbUmaVezSo() {
        service.meta();
        service.meta();

        verify(client, times(1)).providers();
        verify(client, times(1)).genres(TitleType.MOVIE);
    }
}
