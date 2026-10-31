package br.com.watchmatch.catalog.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.watchmatch.catalog.domain.TitleDetail;
import br.com.watchmatch.catalog.domain.TitleRef;
import br.com.watchmatch.catalog.domain.TitleType;
import br.com.watchmatch.catalog.service.TitleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@RestController
public class TitleController {

    private final TitleService service;

    public TitleController(TitleService service) {
        this.service = service;
    }

    @GetMapping("/titles/{mediaType}/{id}")
    public TitleDetail get(@PathVariable String mediaType, @PathVariable int id) {
        return service.get(parseType(mediaType), id);
    }

    @PostMapping("/titles/lookup")
    public LookupResult lookup(@Valid @RequestBody LookupBody body) {
        return new LookupResult(service.lookup(body.titles()));
    }

    // Na URL o tipo vem em minúsculas ("movie" ou "tv")
    private TitleType parseType(String mediaType) {
        return switch (mediaType) {
            case "movie" -> TitleType.MOVIE;
            case "tv" -> TitleType.TV;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mediaType deve ser movie ou tv");
        };
    }

    public record LookupBody(@NotEmpty @Size(max = 20) List<@Valid TitleRef> titles) {
    }

    public record LookupResult(List<TitleDetail> items) {
    }
}
