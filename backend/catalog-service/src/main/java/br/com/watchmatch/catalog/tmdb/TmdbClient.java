package br.com.watchmatch.catalog.tmdb;

import java.util.Map;

import org.springframework.web.client.RestClient;

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
}
