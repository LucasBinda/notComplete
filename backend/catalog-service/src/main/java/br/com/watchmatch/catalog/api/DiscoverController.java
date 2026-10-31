package br.com.watchmatch.catalog.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.watchmatch.catalog.domain.TitleList;
import br.com.watchmatch.catalog.service.DiscoverService;
import jakarta.validation.Valid;

@RestController
public class DiscoverController {

    private final DiscoverService service;

    public DiscoverController(DiscoverService service) {
        this.service = service;
    }

    @PostMapping("/discover")
    public TitleList discover(@Valid @RequestBody DiscoverBody body) {
        return service.discover(body.toDomain(), body.limitOrDefault());
    }
}
