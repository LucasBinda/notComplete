package br.com.watchmatch.llm.groq;

import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Fala com a API do Groq (compatível com a da OpenAI): POST /chat/completions
public class GroqClient {

    private final RestClient restClient;

    public GroqClient(RestClient.Builder builder, String baseUrl, String apiKey) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    // Pede uma resposta em JSON que segue o schema à risca (structured outputs, modo estrito)
    public Completion complete(String model, String system, String user, String schemaName, Map<String, Object> schema) {
        var request = new ChatRequest(
                model,
                List.of(new Message("system", system), new Message("user", user)),
                0.2,
                "low",
                new ResponseFormat("json_schema", new JsonSchema(schemaName, true, schema)));

        ChatResponse response = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(ChatResponse.class);

        String content = response.choices().get(0).message().content();
        Integer tokens = response.usage() != null ? response.usage().totalTokens() : null;
        return new Completion(content, tokens);
    }

    public record Completion(String content, Integer totalTokens) {
    }

    // temperature baixa = respostas mais previsíveis; reasoning_effort "low" = os modelos gpt-oss pensam menos e respondem mais rápido
    record ChatRequest(
            String model,
            List<Message> messages,
            Double temperature,
            @JsonProperty("reasoning_effort") String reasoningEffort,
            @JsonProperty("response_format") ResponseFormat responseFormat) {
    }

    record Message(String role, String content) {
    }

    record ResponseFormat(String type, @JsonProperty("json_schema") JsonSchema jsonSchema) {
    }

    record JsonSchema(String name, Boolean strict, Map<String, Object> schema) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatResponse(List<Choice> choices, Usage usage) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Choice(Message message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Usage(@JsonProperty("total_tokens") Integer totalTokens) {
    }
}
