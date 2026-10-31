package br.com.watchmatch.catalog.tmdb;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import br.com.watchmatch.catalog.domain.DiscoverQueryBuilder;
import br.com.watchmatch.catalog.domain.WatchUrlBuilder;

// Apresenta ao Spring as peças que não dependem dele, para ele entregar a quem precisar
@Configuration
public class TmdbConfig {

    @Bean
    TmdbClient tmdbClient(@Value("${tmdb.api-key}") String apiKey) {
        // Sem timeout, um TMDB lento travaria o serviço inteiro
        var timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(Duration.ofSeconds(3));
        timeouts.setReadTimeout(Duration.ofSeconds(10));
        return new TmdbClient(RestClient.builder().requestFactory(timeouts), apiKey);
    }

    @Bean
    DiscoverQueryBuilder discoverQueryBuilder() {
        return new DiscoverQueryBuilder();
    }

    @Bean
    TitleMapper titleMapper() {
        return new TitleMapper(new WatchUrlBuilder());
    }
}
