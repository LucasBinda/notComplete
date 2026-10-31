package br.com.watchmatch.catalog.domain;

import java.util.List;

// Onde assistir no Brasil. Fonte: JustWatch (via TMDB). link = página "onde assistir" do TMDB
public record WatchInfo(String link, List<WatchProvider> providers) {
}
