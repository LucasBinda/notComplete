package br.com.watchmatch.catalog.service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import br.com.watchmatch.catalog.domain.TitleDetail;
import br.com.watchmatch.catalog.domain.TitleRef;
import br.com.watchmatch.catalog.domain.TitleType;
import br.com.watchmatch.catalog.tmdb.TitleMapper;
import br.com.watchmatch.catalog.tmdb.TmdbClient;

@Service
public class TitleService {

    private final TmdbClient client;
    private final TitleMapper mapper;

    public TitleService(TmdbClient client, TitleMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public TitleDetail get(TitleType type, int id) {
        return mapper.toDetail(client.details(type, id), type);
    }

    // Busca todos ao mesmo tempo (uma thread virtual por título): 20 títulos levam o tempo de ~1
    public List<TitleDetail> lookup(List<TitleRef> refs) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<TitleDetail>> futures = refs.stream()
                    .map(ref -> executor.submit(() -> getOrNull(ref)))
                    .toList();
            return futures.stream()
                    .map(this::await)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    // Id inexistente some da resposta, como diz o contrato
    private TitleDetail getOrNull(TitleRef ref) {
        try {
            return get(ref.mediaType(), ref.id());
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    private TitleDetail await(Future<TitleDetail> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (java.util.concurrent.ExecutionException e) {
            // Repassa o erro original (ex.: TMDB fora do ar), que vira 502 no TmdbErrorHandler
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException(e.getCause());
        }
    }
}
