package br.com.watchmatch.recommendation.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import br.com.watchmatch.recommendation.client.CatalogClient;
import br.com.watchmatch.recommendation.client.LlmClient;
import br.com.watchmatch.recommendation.model.AppliedCriteria;
import br.com.watchmatch.recommendation.model.Catalog.DiscoverRequest;
import br.com.watchmatch.recommendation.model.Catalog.Meta;
import br.com.watchmatch.recommendation.model.Catalog.TitleDetail;
import br.com.watchmatch.recommendation.model.Catalog.TitleList;
import br.com.watchmatch.recommendation.model.Catalog.TitleRef;
import br.com.watchmatch.recommendation.model.Catalog.TitleSummary;
import br.com.watchmatch.recommendation.model.Criteria;
import br.com.watchmatch.recommendation.model.Genre;
import br.com.watchmatch.recommendation.model.Llm.Candidate;
import br.com.watchmatch.recommendation.model.Llm.IntentRequest;
import br.com.watchmatch.recommendation.model.Llm.IntentResponse;
import br.com.watchmatch.recommendation.model.Llm.Pick;
import br.com.watchmatch.recommendation.model.Llm.RerankRequest;
import br.com.watchmatch.recommendation.model.Llm.RerankResponse;
import br.com.watchmatch.recommendation.model.Recommendation;
import br.com.watchmatch.recommendation.model.RecommendRequest;
import br.com.watchmatch.recommendation.model.RecommendResponse;
import br.com.watchmatch.recommendation.model.WatchInfo;

// O fluxo de docs/ARQUITETURA.md seção 4: texto → IA entende → mescla com filtros →
// catálogo busca candidatos reais → IA escolhe e justifica → grounding → detalhes
@Service
public class RecommendService {

    private static final Logger log = LoggerFactory.getLogger(RecommendService.class);

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_PAGE = 10;
    // Quantos candidatos o catálogo traz por página e quantos vão para a IA escolher.
    // A IA recebe menos para caber no limite de tokens por minuto do Groq gratuito.
    private static final int CATALOG_LIMIT = 40;
    private static final int RERANK_CANDIDATES = 30;
    private static final int OVERVIEW_MAX = 300;

    static final String OFF_TOPIC = "Não entendi isso como um pedido de filme ou série; mostrando títulos populares.";
    static final String RELAXED = "Não achei nada com tudo o que entendi do pedido; mostrando resultados só com os seus filtros.";
    static final String NOTHING_FOUND = "Nenhum título encontrado nas suas plataformas com esses critérios.";

    private final CatalogClient catalog;
    private final LlmClient llm;
    private final TextSanitizer sanitizer;
    private final CriteriaMerger merger;

    public RecommendService(CatalogClient catalog, LlmClient llm, TextSanitizer sanitizer, CriteriaMerger merger) {
        this.catalog = catalog;
        this.llm = llm;
        this.sanitizer = sanitizer;
        this.merger = merger;
    }

    public RecommendResponse recommend(RecommendRequest request, String requestId) {
        int limit = request.limit() != null ? request.limit() : DEFAULT_LIMIT;
        int page = request.page() != null ? request.page() : 1;
        String text = sanitizer.clean(request.query());
        List<String> notices = new ArrayList<>();
        boolean degraded = false;

        // 1. A IA transforma o texto em critérios
        IntentResponse intent = null;
        if (text != null) {
            try {
                intent = llm.intent(intentRequest(text));
            } catch (RestClientException e) {
                // Sem aviso em notices: o frontend já avisa quando degraded = true
                log.warn("llm-service /intent falhou: {}", e.getMessage());
                degraded = true;
            }
            if (intent != null && Boolean.TRUE.equals(intent.offTopic())) {
                notices.add(OFF_TOPIC);
                intent = null;
            }
        }
        String summary = intent != null ? intent.summary() : null;

        // 2. Mescla com os filtros da tela e busca candidatos reais no catálogo
        MergedCriteria merged = merger.merge(request.filters(), intent, request.providers());
        TitleList found = discover(merged, page);
        if (isEmpty(found) && intent != null) {
            // A IA pode ter entendido critérios demais: tenta só com o que o usuário marcou
            merged = merger.merge(request.filters(), null, request.providers());
            found = discover(merged, page);
            notices.add(RELAXED);
            summary = null;
        }
        List<TitleSummary> candidates = isEmpty(found) ? List.of() : found.items();
        if (candidates.isEmpty()) {
            notices.add(NOTHING_FOUND);
        }

        // 3. A IA escolhe os melhores entre os candidatos e explica o porquê
        List<Pick> picks = null;
        if (intent != null && !candidates.isEmpty()) {
            try {
                RerankResponse rerank = llm.rerank(new RerankRequest(text, intent.summary(),
                        toCandidates(candidates), limit));
                picks = ground(rerank.picks(), candidates, limit);
            } catch (RestClientException e) {
                log.warn("llm-service /rerank falhou: {}", e.getMessage());
                degraded = true;
            }
        }
        // Sem IA (ou se ela não escolheu nada válido): a ordem do catálogo
        if (picks == null || picks.isEmpty()) {
            picks = candidates.stream().limit(limit).map(c -> new Pick(c.id(), c.mediaType(), null)).toList();
        }

        // 4. Detalhes (duração, classificação, onde assistir) só dos escolhidos
        List<Recommendation> items = details(picks, request.providers());

        var criteria = new AppliedCriteria(merged.criteria(), merged.providers(),
                intent != null ? intent.referenceTitle() : null, summary, merged.sources());
        boolean hasMore = found != null && Boolean.TRUE.equals(found.hasMore()) && page < MAX_PAGE;
        return new RecommendResponse(requestId, page, hasMore, criteria, items, degraded, notices);
    }

    private IntentRequest intentRequest(String text) {
        Meta meta = catalog.meta();
        // Gêneros de filme e de série juntos, sem repetir id
        Map<Integer, Genre> genres = new LinkedHashMap<>();
        Stream.concat(meta.genres().movie().stream(), meta.genres().tv().stream())
                .forEach(g -> genres.putIfAbsent(g.id(), g));
        return new IntentRequest(text, List.copyOf(genres.values()), meta.providers());
    }

    private TitleList discover(MergedCriteria merged, int page) {
        Criteria criteria = merged.criteria() != null ? merged.criteria() : Criteria.empty();
        return catalog.discover(new DiscoverRequest(criteria, merged.providers(), CATALOG_LIMIT, page));
    }

    private boolean isEmpty(TitleList list) {
        return list == null || list.items() == null || list.items().isEmpty();
    }

    private List<Candidate> toCandidates(List<TitleSummary> titles) {
        return titles.stream()
                .limit(RERANK_CANDIDATES)
                .map(t -> new Candidate(t.id(), t.mediaType(), t.title(), t.year(), shorten(t.overview()),
                        t.genres() == null ? List.of() : t.genres().stream().map(Genre::name).toList()))
                .toList();
    }

    private String shorten(String text) {
        if (text == null || text.length() <= OVERVIEW_MAX) {
            return text;
        }
        return text.substring(0, OVERVIEW_MAX - 1) + "…";
    }

    // Grounding: só vale o que veio do catálogo. Id inventado ou repetido é descartado.
    private List<Pick> ground(List<Pick> picks, List<TitleSummary> candidates, int limit) {
        if (picks == null) {
            return List.of();
        }
        Set<String> real = candidates.stream().map(c -> key(c.mediaType(), c.id())).collect(Collectors.toSet());
        Set<String> used = new LinkedHashSet<>();
        List<Pick> grounded = picks.stream()
                .filter(p -> real.contains(key(p.mediaType(), p.id())))
                .filter(p -> used.add(key(p.mediaType(), p.id())))
                .limit(limit)
                .toList();
        int discarded = picks.size() - grounded.size();
        if (discarded > 0) {
            log.info("grounding descartou {} escolha(s) da IA fora dos candidatos", discarded);
        }
        return grounded;
    }

    private List<Recommendation> details(List<Pick> picks, List<Integer> userProviders) {
        List<TitleRef> refs = picks.stream().map(p -> new TitleRef(p.mediaType(), p.id())).toList();
        Map<String, TitleDetail> byKey = catalog.lookup(refs).stream()
                .collect(Collectors.toMap(d -> key(d.mediaType(), d.id()), Function.identity(), (a, b) -> a));

        return picks.stream()
                .filter(p -> byKey.containsKey(key(p.mediaType(), p.id())))
                .map(p -> toRecommendation(byKey.get(key(p.mediaType(), p.id())), p.reason(), userProviders))
                .toList();
    }

    private Recommendation toRecommendation(TitleDetail d, String reason, List<Integer> userProviders) {
        // Mostra só as plataformas que o usuário assina
        WatchInfo watch = d.watch() == null
                ? new WatchInfo(null, List.of())
                : new WatchInfo(d.watch().link(), d.watch().providers() == null ? List.of()
                        : d.watch().providers().stream().filter(p -> userProviders.contains(p.id())).toList());
        return new Recommendation(d.id(), d.mediaType(), d.title(), d.originalTitle(), d.year(), d.overview(),
                d.posterUrl(), d.backdropUrl(), d.genres(), d.voteAverage(), d.runtime(), d.seasons(),
                d.certification(), watch, reason);
    }

    private static String key(String mediaType, Integer id) {
        return mediaType + "-" + id;
    }
}
