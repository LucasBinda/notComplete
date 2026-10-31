package br.com.watchmatch.recommendation.client;

import java.util.List;

import org.springframework.web.client.RestClient;

import br.com.watchmatch.recommendation.model.Catalog.DiscoverRequest;
import br.com.watchmatch.recommendation.model.Catalog.LookupRequest;
import br.com.watchmatch.recommendation.model.Catalog.LookupResult;
import br.com.watchmatch.recommendation.model.Catalog.Meta;
import br.com.watchmatch.recommendation.model.Catalog.TitleDetail;
import br.com.watchmatch.recommendation.model.Catalog.TitleList;
import br.com.watchmatch.recommendation.model.Catalog.TitleRef;

// Fala com o catalog-service (contracts/catalog-service.yaml)
public class CatalogClient {

    private final RestClient rest;

    public CatalogClient(RestClient rest) {
        this.rest = rest;
    }

    public Meta meta() {
        return rest.get().uri("/meta").retrieve().body(Meta.class);
    }

    public TitleList discover(DiscoverRequest request) {
        return rest.post().uri("/discover").body(request).retrieve().body(TitleList.class);
    }

    public List<TitleDetail> lookup(List<TitleRef> refs) {
        if (refs.isEmpty()) {
            return List.of();
        }
        return rest.post().uri("/titles/lookup").body(new LookupRequest(refs)).retrieve()
                .body(LookupResult.class).items();
    }
}
