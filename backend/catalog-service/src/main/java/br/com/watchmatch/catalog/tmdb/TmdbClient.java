package br.com.watchmatch.catalog.tmdb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.client.RestClient;

import br.com.watchmatch.catalog.domain.Genre;
import br.com.watchmatch.catalog.domain.TitleType;

public class TmdbClient {

    private static final String BASE_URL = "https://api.themoviedb.org/3";

    private final RestClient restClient;

    // A chave é o token v4 do TMDB, que vai no cabeçalho e não na URL
    public TmdbClient(RestClient.Builder builder, String apiKey) {
        this.restClient = builder
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public TmdbPage discover(TitleType type, Map<String, String> params) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/discover/" + type.name().toLowerCase());
                    params.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(TmdbPage.class);
    }

    // id → nome em português (ex.: 35 → "Comédia")
    public Map<Integer, String> genres(TitleType type) {
        TmdbGenreList list = restClient.get()
                .uri("/genre/{type}/list?language=pt-BR", type.name().toLowerCase())
                .retrieve()
                .body(TmdbGenreList.class);

        return list.genres().stream().collect(Collectors.toMap(Genre::id, Genre::name));
    }

    // Uma chamada só traz detalhes + onde assistir + elenco + classificação
    public TmdbDetail details(TitleType type, int id) {
        String certifications = type == TitleType.MOVIE ? "release_dates" : "content_ratings";
        return restClient.get()
                .uri("/{type}/{id}?language=pt-BR&append_to_response=watch/providers,credits,{certifications}",
                        type.name().toLowerCase(), id, certifications)
                .retrieve()
                .body(TmdbDetail.class);
    }

    // Plataformas com catálogo no Brasil (a lista de filmes cobre as de séries também)
    public List<TmdbProvider> providers() {
        TmdbProviderList list = restClient.get()
                .uri("/watch/providers/movie?watch_region=BR&language=pt-BR")
                .retrieve()
                .body(TmdbProviderList.class);

        return list.results();
    }
}
