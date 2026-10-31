package br.com.watchmatch.catalog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import br.com.watchmatch.catalog.domain.DiscoverQueryBuilder;
import br.com.watchmatch.catalog.domain.DiscoverRequest;
import br.com.watchmatch.catalog.domain.TitleList;
import br.com.watchmatch.catalog.domain.TitleSummary;
import br.com.watchmatch.catalog.domain.TitleType;
import br.com.watchmatch.catalog.tmdb.TitleMapper;
import br.com.watchmatch.catalog.tmdb.TmdbClient;
import br.com.watchmatch.catalog.tmdb.TmdbPage;

@Service
public class DiscoverService {

    private final TmdbClient client;
    private final DiscoverQueryBuilder queryBuilder;
    private final TitleMapper mapper;

    // A lista de gêneros quase nunca muda: busca uma vez por tipo e guarda
    private final Map<TitleType, Map<Integer, String>> genreNames = new ConcurrentHashMap<>();

    public DiscoverService(TmdbClient client, DiscoverQueryBuilder queryBuilder, TitleMapper mapper) {
        this.client = client;
        this.queryBuilder = queryBuilder;
        this.mapper = mapper;
    }

    public TitleList discover(DiscoverRequest request, int limit) {
        // Sem mediaType, busca os dois tipos
        List<TitleType> types = request.mediaType() != null
                ? List.of(request.mediaType())
                : List.of(TitleType.MOVIE, TitleType.TV);

        List<List<TitleSummary>> perType = new ArrayList<>();
        boolean hasMore = false;

        for (TitleType type : types) {
            TmdbPage page = client.discover(type, queryBuilder.build(request, type));
            perType.add(toSummaries(page, type));
            hasMore |= hasNextPage(page);
        }

        List<TitleSummary> items = interleave(perType);
        return new TitleList(items.subList(0, Math.min(limit, items.size())), hasMore);
    }

    private List<TitleSummary> toSummaries(TmdbPage page, TitleType type) {
        if (page.results() == null) {
            return List.of();
        }
        Map<Integer, String> names = genreNames.computeIfAbsent(type, client::genres);
        return page.results().stream()
                .map(result -> mapper.toSummary(result, type, names))
                .toList();
    }

    private boolean hasNextPage(TmdbPage page) {
        return page.page() != null && page.totalPages() != null && page.page() < page.totalPages();
    }

    // [F1, F2, F3] + [S1] vira [F1, S1, F2, F3]
    private List<TitleSummary> interleave(List<List<TitleSummary>> lists) {
        List<TitleSummary> result = new ArrayList<>();
        int longest = lists.stream().mapToInt(List::size).max().orElse(0);
        for (int i = 0; i < longest; i++) {
            for (List<TitleSummary> list : lists) {
                if (i < list.size()) {
                    result.add(list.get(i));
                }
            }
        }
        return result;
    }
}
