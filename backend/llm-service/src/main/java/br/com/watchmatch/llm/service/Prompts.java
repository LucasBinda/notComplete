package br.com.watchmatch.llm.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

// Peças comuns aos dois serviços de IA
final class Prompts {

    static final JsonMapper JSON = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private Prompts() {
    }

    // O texto do usuário vai entre marcadores, longe das regras. Tiramos os marcadores de dentro
    // dele para ninguém "fechar" o bloco e escrever instruções depois.
    static String userBlock(String text) {
        String safe = text == null ? "" : text.replace("<pedido>", "").replace("</pedido>", "");
        return "<pedido>\n" + safe + "\n</pedido>";
    }

    static <T> T parse(String content, Class<T> type) {
        if (content == null) {
            throw new LlmOutputException("A IA não devolveu conteúdo", null);
        }
        try {
            return JSON.readValue(content, type);
        } catch (JacksonException e) {
            throw new LlmOutputException("A IA devolveu um JSON inválido", e);
        }
    }

    static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
