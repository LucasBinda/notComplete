package br.com.watchmatch.llm.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.client.ResourceAccessException;

import br.com.watchmatch.llm.service.IntentService;
import br.com.watchmatch.llm.service.LlmOutputException;
import br.com.watchmatch.llm.service.RerankService;

/**
 * As rotas /intent e /rerank: validação do pedido e erros no formato do contrato.
 */
@WebMvcTest(LlmController.class)
class LlmControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private IntentService intentService;

    @MockitoBean
    private RerankService rerankService;

    @Test
    void textoVazioDevolve400() {
        var resposta = mvc.post().uri("/intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\": \"\", \"genres\": [], \"providers\": []}")
                .exchange();

        assertThat(resposta).hasStatus(400);
    }

    @Test
    void rerankSemCandidatosDevolve400() {
        var resposta = mvc.post().uri("/rerank")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\": \"x\", \"candidates\": []}")
                .exchange();

        assertThat(resposta).hasStatus(400);
    }

    @Test
    void groqForaDoArDevolve502() {
        when(intentService.interpret(any())).thenThrow(new ResourceAccessException("timeout"));

        var resposta = mvc.post().uri("/intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\": \"comédia\", \"genres\": [], \"providers\": []}")
                .exchange();

        assertThat(resposta).hasStatus(502);
        assertThat(resposta).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void respostaInvalidaDaIaDevolve502() {
        when(intentService.interpret(any())).thenThrow(new LlmOutputException("json quebrado", null));

        var resposta = mvc.post().uri("/intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\": \"comédia\", \"genres\": [], \"providers\": []}")
                .exchange();

        assertThat(resposta).hasStatus(502);
    }
}
