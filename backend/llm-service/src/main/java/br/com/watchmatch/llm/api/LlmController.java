package br.com.watchmatch.llm.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.watchmatch.llm.model.Intent;
import br.com.watchmatch.llm.model.Rerank;
import br.com.watchmatch.llm.service.IntentService;
import br.com.watchmatch.llm.service.RerankService;
import jakarta.validation.Valid;

@RestController
public class LlmController {

    private final IntentService intentService;
    private final RerankService rerankService;

    public LlmController(IntentService intentService, RerankService rerankService) {
        this.intentService = intentService;
        this.rerankService = rerankService;
    }

    @PostMapping("/intent")
    public Intent.Response intent(@Valid @RequestBody Intent.Request request) {
        return intentService.interpret(request);
    }

    @PostMapping("/rerank")
    public Rerank.Response rerank(@Valid @RequestBody Rerank.Request request) {
        return rerankService.rerank(request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
