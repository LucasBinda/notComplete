package br.com.watchmatch.llm.model;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// POST /intent (llm-service.yaml)
public final class Intent {

    private Intent() {
    }

    public record Request(
            @NotBlank @Size(max = 500) String text,
            @NotNull List<Genre> genres,
            @NotNull List<Provider> providers) {
    }

    public record Response(
            Criteria criteria,
            List<Integer> providers,
            String referenceTitle,
            String summary,
            Boolean offTopic,
            LlmMeta meta) {
    }
}
