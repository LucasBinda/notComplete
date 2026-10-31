package br.com.watchmatch.recommendation.service;

import org.springframework.stereotype.Component;

// Limpa o texto antes de ir para a IA: menos lixo = menos tokens e menos espaço para truques
@Component
public class TextSanitizer {

    private static final int MAX_LENGTH = 500;

    public String clean(String text) {
        if (text == null) {
            return null;
        }
        String cleaned = text
                .replaceAll("\\s+", " ")                // quebras de linha e tabs viram um espaço
                .replaceAll("[\\p{Cc}\\p{Cf}]", "")     // caracteres de controle e invisíveis
                .replaceAll(" {2,}", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned.length() > MAX_LENGTH ? cleaned.substring(0, MAX_LENGTH) : cleaned;
    }
}
