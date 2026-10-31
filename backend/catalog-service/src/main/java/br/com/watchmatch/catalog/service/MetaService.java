package br.com.watchmatch.catalog.service;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.watchmatch.catalog.domain.Genre;
import br.com.watchmatch.catalog.domain.Meta;
import br.com.watchmatch.catalog.domain.Provider;
import br.com.watchmatch.catalog.domain.TitleType;
import br.com.watchmatch.catalog.tmdb.TmdbClient;
import br.com.watchmatch.catalog.tmdb.TmdbProvider;

@Service
public class MetaService {

    private static final String LOGO_BASE_URL = "https://image.tmdb.org/t/p/w92";

    private final TmdbClient client;
    // O TMDB lista dezenas de plataformas (lojas, canais...); o site oferece só as de assinatura escolhidas aqui
    private final List<Integer> providerIds;

    // Gêneros e plataformas quase nunca mudam: busca uma vez e guarda
    private volatile Meta cached;

    public MetaService(TmdbClient client, @Value("${catalog.providers}") List<Integer> providerIds) {
        this.client = client;
        this.providerIds = providerIds;
    }

    public Meta meta() {
        if (cached == null) {
            cached = new Meta(
                    new Meta.Genres(sortedGenres(TitleType.MOVIE), sortedGenres(TitleType.TV)),
                    configuredProviders());
        }
        return cached;
    }

    private List<Genre> sortedGenres(TitleType type) {
        // Collator ordena como em português ("Ação" antes de "Animação")
        Collator collator = Collator.getInstance(Locale.of("pt", "BR"));
        return client.genres(type).entrySet().stream()
                .map(e -> new Genre(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(Genre::name, collator))
                .toList();
    }

    private List<Provider> configuredProviders() {
        Map<Integer, TmdbProvider> byId = client.providers().stream()
                .collect(Collectors.toMap(TmdbProvider::providerId, Function.identity(), (a, b) -> a));
        return providerIds.stream()
                .filter(byId::containsKey)
                .map(byId::get)
                .map(p -> new Provider(p.providerId(), p.providerName(), LOGO_BASE_URL + p.logoPath()))
                .toList();
    }
}
