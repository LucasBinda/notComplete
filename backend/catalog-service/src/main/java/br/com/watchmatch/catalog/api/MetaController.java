package br.com.watchmatch.catalog.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.watchmatch.catalog.domain.Meta;
import br.com.watchmatch.catalog.service.MetaService;

@RestController
public class MetaController {

    private final MetaService service;

    public MetaController(MetaService service) {
        this.service = service;
    }

    @GetMapping("/meta")
    public Meta meta() {
        return service.meta();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
