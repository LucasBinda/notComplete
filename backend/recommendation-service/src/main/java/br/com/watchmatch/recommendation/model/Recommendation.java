package br.com.watchmatch.recommendation.model;

import java.util.List;

// Um card de resultado. reason = justificativa da IA (null quando a IA não foi usada)
public record Recommendation(
        Integer id,
        String mediaType,
        String title,
        String originalTitle,
        Integer year,
        String overview,
        String posterUrl,
        String backdropUrl,
        List<Genre> genres,
        Double voteAverage,
        Integer runtime,
        Integer seasons,
        String certification,
        WatchInfo watch,
        String reason) {
}
