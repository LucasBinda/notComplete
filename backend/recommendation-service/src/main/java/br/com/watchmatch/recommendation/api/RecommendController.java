package br.com.watchmatch.recommendation.api;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.watchmatch.recommendation.model.RecommendRequest;
import br.com.watchmatch.recommendation.model.RecommendResponse;
import br.com.watchmatch.recommendation.service.RecommendService;
import jakarta.validation.Valid;

@RestController
public class RecommendController {

    private final RecommendService service;

    public RecommendController(RecommendService service) {
        this.service = service;
    }

    @PostMapping("/recommend")
    public RecommendResponse recommend(@Valid @RequestBody RecommendRequest request) {
        return service.recommend(request, MDC.get(RequestIdFilter.MDC_KEY));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
