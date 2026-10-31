package br.com.watchmatch.llm.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

// Lê os prompts versionados de src/main/resources/prompts/ (ex.: intent.v1.md)
@Component
public class PromptLoader {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String load(String version) {
        return cache.computeIfAbsent(version, this::read);
    }

    private String read(String version) {
        String path = "/prompts/" + version + ".md";
        try (InputStream in = PromptLoader.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Prompt não encontrado: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
