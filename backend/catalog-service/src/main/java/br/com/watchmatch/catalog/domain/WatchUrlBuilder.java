package br.com.watchmatch.catalog.domain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WatchUrlBuilder {

    // provider_id → endereço de busca da plataforma (ver docs/APIS.md).
    // Outras plataformas entram aqui quando o modelo de busca delas for testado.
    private static final Map<Integer, String> SEARCH_URLS = Map.of(
            8, "https://www.netflix.com/search?q=",
            119, "https://www.primevideo.com/search?phrase=");

    public String build(Integer providerId, String title, String fallbackLink) {
        String searchUrl = SEARCH_URLS.get(providerId);
        if (searchUrl == null) {
            return fallbackLink;
        }
        // URLEncoder troca espaço por "+"; %20 funciona em qualquer site
        return searchUrl + URLEncoder.encode(title, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
