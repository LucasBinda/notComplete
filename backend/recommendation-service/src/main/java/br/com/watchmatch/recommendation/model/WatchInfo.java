package br.com.watchmatch.recommendation.model;

import java.util.List;

// Onde assistir no Brasil. Fonte: JustWatch (via TMDB)
public record WatchInfo(String link, List<WatchProvider> providers) {
}
