package br.com.watchmatch.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Limpa o texto do usuário antes de mandar para a IA.
 */
class TextSanitizerTest {

    private final TextSanitizer sanitizer = new TextSanitizer();

    @Test
    void tiraEspacosSobrandoEQuebrasDeLinha() {
        assertThat(sanitizer.clean("  comédia\n\n  curta\t para   hoje  ")).isEqualTo("comédia curta para hoje");
    }

    @Test
    void tiraCaracteresDeControleInvisiveis() {
        assertThat(sanitizer.clean("terror\u0000\u0007 leve​")).isEqualTo("terror leve");
    }

    @Test
    void cortaEm500Caracteres() {
        assertThat(sanitizer.clean("a".repeat(800))).hasSize(500);
    }

    @Test
    void textoVazioOuNullViraNull() {
        assertThat(sanitizer.clean("   ")).isNull();
        assertThat(sanitizer.clean(null)).isNull();
    }
}
