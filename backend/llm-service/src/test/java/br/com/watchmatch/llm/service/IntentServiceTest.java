package br.com.watchmatch.llm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.com.watchmatch.llm.groq.GroqClient;
import br.com.watchmatch.llm.groq.GroqClient.Completion;
import br.com.watchmatch.llm.model.Genre;
import br.com.watchmatch.llm.model.Intent;
import br.com.watchmatch.llm.model.Provider;

/**
 * /intent: o texto do usuário vira critérios. O Groq é falso; aqui testamos o que fazemos
 * antes (montar o schema só com ids permitidos) e depois (validar o que a IA respondeu).
 */
class IntentServiceTest {

    private static final String MODELO = "openai/gpt-oss-20b";

    private final GroqClient groq = mock(GroqClient.class);
    private final IntentService service = new IntentService(groq, new PromptLoader(), MODELO);

    private final Intent.Request pedido = new Intent.Request(
            "comédia leve na Netflix, até 2h, nada de terror",
            List.of(new Genre(35, "Comédia"), new Genre(27, "Terror")),
            List.of(new Provider(8, "Netflix", null), new Provider(119, "Amazon Prime Video", null)));

    @Test
    void respostaDaIaViraCriterios() {
        responde("""
            {"offTopic": false, "mediaType": "movie", "genres": [35], "excludeGenres": [27],
             "runtimeMax": 120, "yearFrom": 0, "yearTo": 0, "originalLanguage": "", "certificationMax": "",
             "keywords": ["light comedy"], "providers": [8], "referenceTitle": "", "summary": "Comédia leve, até 2h"}
            """);

        Intent.Response resposta = service.interpret(pedido);

        assertThat(resposta.offTopic()).isFalse();
        assertThat(resposta.criteria().mediaType()).isEqualTo("movie");
        assertThat(resposta.criteria().genres()).containsExactly(35);
        assertThat(resposta.criteria().excludeGenres()).containsExactly(27);
        assertThat(resposta.criteria().runtimeMax()).isEqualTo(120);
        assertThat(resposta.criteria().keywords()).containsExactly("light comedy");
        assertThat(resposta.providers()).containsExactly(8);
        assertThat(resposta.summary()).isEqualTo("Comédia leve, até 2h");
        assertThat(resposta.meta().model()).isEqualTo(MODELO);
        assertThat(resposta.meta().promptVersion()).isEqualTo("intent.v1");
        assertThat(resposta.meta().totalTokens()).isEqualTo(500);
    }

    @Test
    void valoresDeNaoSeiViramNull() {
        // O modo estrito não deixa a IA mandar null; ela usa 0, "" e "any" no lugar
        responde("""
            {"offTopic": false, "mediaType": "any", "genres": [], "excludeGenres": [],
             "runtimeMax": 0, "yearFrom": 0, "yearTo": 0, "originalLanguage": "", "certificationMax": "",
             "keywords": [], "providers": [], "referenceTitle": "", "summary": "Algo para hoje"}
            """);

        Intent.Response resposta = service.interpret(pedido);

        assertThat(resposta.criteria().mediaType()).isNull();
        assertThat(resposta.criteria().genres()).isNull();
        assertThat(resposta.criteria().runtimeMax()).isNull();
        assertThat(resposta.criteria().yearFrom()).isNull();
        assertThat(resposta.criteria().originalLanguage()).isNull();
        assertThat(resposta.criteria().certificationMax()).isNull();
        assertThat(resposta.referenceTitle()).isNull();
    }

    @Test
    void idsForaDasListasEValoresAbsurdosSaoDescartados() {
        responde("""
            {"offTopic": false, "mediaType": "movie", "genres": [35, 9999], "excludeGenres": [],
             "runtimeMax": -5, "yearFrom": 1200, "yearTo": 2010, "originalLanguage": "coreano",
             "certificationMax": "PG-13", "keywords": ["a", "b", "c", "d", "e", "f"], "providers": [8, 337],
             "referenceTitle": "Interestelar", "summary": "Resumo"}
            """);

        Intent.Response resposta = service.interpret(pedido);

        assertThat(resposta.criteria().genres()).containsExactly(35);
        assertThat(resposta.criteria().runtimeMax()).isNull();
        assertThat(resposta.criteria().yearFrom()).isNull();
        assertThat(resposta.criteria().yearTo()).isEqualTo(2010);
        assertThat(resposta.criteria().originalLanguage()).isNull();
        assertThat(resposta.criteria().certificationMax()).isNull();
        assertThat(resposta.criteria().keywords()).hasSize(5);
        assertThat(resposta.providers()).containsExactly(8);
        assertThat(resposta.referenceTitle()).isEqualTo("Interestelar");
    }

    @Test
    void pedidoForaDoTemaVoltaSemCriterios() {
        responde("""
            {"offTopic": true, "mediaType": "movie", "genres": [35], "excludeGenres": [],
             "runtimeMax": 0, "yearFrom": 0, "yearTo": 0, "originalLanguage": "", "certificationMax": "",
             "keywords": [], "providers": [8], "referenceTitle": "", "summary": "Poema"}
            """);

        Intent.Response resposta = service.interpret(pedido);

        assertThat(resposta.offTopic()).isTrue();
        assertThat(resposta.criteria().genres()).isNull();
        assertThat(resposta.providers()).isEmpty();
    }

    @Test
    void schemaSoDeixaEscolherOsIdsEnviados() {
        responde("""
            {"offTopic": false, "mediaType": "any", "genres": [], "excludeGenres": [], "runtimeMax": 0,
             "yearFrom": 0, "yearTo": 0, "originalLanguage": "", "certificationMax": "", "keywords": [],
             "providers": [], "referenceTitle": "", "summary": ""}
            """);

        service.interpret(pedido);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> schema = ArgumentCaptor.forClass(Map.class);
        var mensagem = ArgumentCaptor.forClass(String.class);
        verify(groq).complete(eq(MODELO), anyString(), mensagem.capture(), eq("intent"), schema.capture());
        assertThat(schema.getValue().toString()).contains("enum=[35, 27]").contains("enum=[8, 119]");
        // O texto do usuário vai entre marcadores, separado das regras
        assertThat(mensagem.getValue()).contains("<pedido>\ncomédia leve na Netflix, até 2h, nada de terror\n</pedido>");
    }

    @Test
    void jsonQuebradoDaIaViraErro() {
        responde("isso não é json");

        assertThatThrownBy(() -> service.interpret(pedido)).isInstanceOf(LlmOutputException.class);
    }

    private void responde(String json) {
        when(groq.complete(eq(MODELO), anyString(), anyString(), eq("intent"), any()))
                .thenReturn(new Completion(json, 500));
    }
}
