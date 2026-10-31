package br.com.watchmatch.llm.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import br.com.watchmatch.llm.service.LlmOutputException;

// Falhas do Groq viram 502, como pede o contrato. O recommendation-service segue sem a IA.
@RestControllerAdvice
public class LlmErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(LlmErrorHandler.class);

    @ExceptionHandler(RestClientException.class)
    public ProblemDetail groqFailed(RestClientException e) {
        log.warn("Groq falhou: {}", e.getMessage());
        return problem("A IA (Groq) não respondeu.");
    }

    @ExceptionHandler(LlmOutputException.class)
    public ProblemDetail invalidOutput(LlmOutputException e) {
        log.warn("Resposta inválida da IA: {}", e.getMessage());
        return problem("A IA respondeu num formato inválido.");
    }

    private ProblemDetail problem(String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, detail);
        problem.setTitle("Falha na IA");
        return problem;
    }
}
