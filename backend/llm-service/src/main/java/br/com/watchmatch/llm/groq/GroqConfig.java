package br.com.watchmatch.llm.groq;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class GroqConfig {

    @Bean
    GroqClient groqClient(@Value("${groq.url}") String url, @Value("${groq.api-key}") String apiKey) {
        var timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(Duration.ofSeconds(3));
        timeouts.setReadTimeout(Duration.ofSeconds(20));
        return new GroqClient(RestClient.builder().requestFactory(timeouts), url, apiKey);
    }
}
