package br.com.watchmatch.recommendation.model;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

// POST /recommend (recommendation-service.yaml)
public record RecommendRequest(
        @Size(max = 500) String query,
        Criteria filters,
        @NotEmpty List<Integer> providers,
        @Min(1) @Max(20) Integer limit,
        @Min(1) @Max(10) Integer page) {
}
