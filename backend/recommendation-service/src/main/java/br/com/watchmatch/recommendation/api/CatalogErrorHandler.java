package br.com.watchmatch.recommendation.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

// Falhas da IA são tratadas no RecommendService (o site continua sem ela).
// Sem o catálogo não há recomendação possível: respondemos 502, como pede o contrato.
@RestControllerAdvice
public class CatalogErrorHandler {

    @ExceptionHandler(RestClientException.class)
    public ProblemDetail catalogFailed(RestClientException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "O catálogo de filmes não respondeu. Tente de novo em instantes.");
        problem.setTitle("Catálogo indisponível");
        return problem;
    }
}
