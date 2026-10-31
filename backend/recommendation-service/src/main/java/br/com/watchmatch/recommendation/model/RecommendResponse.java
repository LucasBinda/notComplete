package br.com.watchmatch.recommendation.model;

import java.util.List;

public record RecommendResponse(
        String requestId,
        Integer page,
        Boolean hasMore,
        AppliedCriteria criteria,
        List<Recommendation> items,
        Boolean degraded,
        List<String> notices) {
}
