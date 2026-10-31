package br.com.watchmatch.llm.service;

// A IA respondeu algo que não dá para usar (ex.: JSON quebrado)
public class LlmOutputException extends RuntimeException {

    public LlmOutputException(String message, Throwable cause) {
        super(message, cause);
    }
}
