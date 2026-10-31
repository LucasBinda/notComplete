package br.com.watchmatch.recommendation.client;

import org.springframework.web.client.RestClient;

import br.com.watchmatch.recommendation.model.Llm.IntentRequest;
import br.com.watchmatch.recommendation.model.Llm.IntentResponse;
import br.com.watchmatch.recommendation.model.Llm.RerankRequest;
import br.com.watchmatch.recommendation.model.Llm.RerankResponse;

// Fala com o llm-service (contracts/llm-service.yaml)
public class LlmClient {

    private final RestClient rest;

    public LlmClient(RestClient rest) {
        this.rest = rest;
    }

    public IntentResponse intent(IntentRequest request) {
        return rest.post().uri("/intent").body(request).retrieve().body(IntentResponse.class);
    }

    public RerankResponse rerank(RerankRequest request) {
        return rest.post().uri("/rerank").body(request).retrieve().body(RerankResponse.class);
    }
}
