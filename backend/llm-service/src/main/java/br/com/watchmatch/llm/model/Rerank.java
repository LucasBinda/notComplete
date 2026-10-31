package br.com.watchmatch.llm.model;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

// POST /rerank (llm-service.yaml)
public final class Rerank {

    private Rerank() {
    }

    public record Request(
            @Size(max = 500) String text,
            @Size(max = 120) String summary,
            @NotEmpty @Size(max = 60) List<@Valid Candidate> candidates,
            @Min(1) @Max(20) Integer limit) {
    }

    public record Candidate(
            Integer id,
            String mediaType,
            String title,
            Integer year,
            @Size(max = 600) String overview,
            List<String> genres) {
    }

    public record Pick(Integer id, String mediaType, String reason) {
    }

    // discarded = quantas escolhas da IA estavam fora da lista de candidatos (métrica de alucinação)
    public record Response(List<Pick> picks, Integer discarded, LlmMeta meta) {
    }
}
