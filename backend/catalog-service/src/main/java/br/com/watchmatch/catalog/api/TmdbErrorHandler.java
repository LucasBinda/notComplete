package br.com.watchmatch.catalog.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

// Quando o TMDB falha (fora do ar, chave errada, demora), respondemos 502 como pede o contrato
@RestControllerAdvice
public class TmdbErrorHandler {

    // Título que não existe no TMDB
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ProblemDetail notFound(HttpClientErrorException.NotFound e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Título não encontrado no TMDB.");
        problem.setTitle("Não encontrado");
        return problem;
    }

    @ExceptionHandler(RestClientException.class)
    public ProblemDetail tmdbFailed(RestClientException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "O catálogo de filmes (TMDB) não respondeu. Tente de novo em instantes.");
        problem.setTitle("Falha no TMDB");
        return problem;
    }
}
