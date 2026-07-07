package springboot_application.featureflag.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client for the Convocatorias AI Service (Python/FastAPI).
 * Calls document processing, convocatoria generation, and chatbot endpoints.
 * Uses Resilience4j CircuitBreaker for fault tolerance.
 */
@Service
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.service.url:http://convocatorias-ai:8000}")
    private String aiServiceUrl;

    @Value("${ai.service.api-key:default-key}")
    private String apiKey;

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);
        return headers;
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackProcessDocument")
    public String processDocument(String documentBase64, boolean useLlm) {
        String url = aiServiceUrl + "/process-document";
        Map<String, Object> body = Map.of(
                "document", documentBase64,
                "use_llm", useLlm
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, buildHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }

    public String fallbackProcessDocument(String documentBase64, boolean useLlm, Throwable throwable) {
        log.error("AI service unavailable for document processing: {}", throwable.getMessage());
        return "{\"error\":\"AI service unavailable\", \"fallback\": true}";
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackGenerate")
    public String generateConvocatoria(Map<String, Object> entities, boolean useLlm) {
        String url = aiServiceUrl + "/generate?use_llm=" + useLlm;
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(entities, buildHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }

    public String fallbackGenerate(Map<String, Object> entities, boolean useLlm, Throwable throwable) {
        log.error("AI service unavailable for generation: {}", throwable.getMessage());
        return "{\"error\":\"AI service unavailable\", \"fallback\": true}";
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackChat")
    public String chat(String sessionId, String message, boolean useLlm) {
        String url = aiServiceUrl + "/chat";
        Map<String, Object> body = Map.of(
                "session_id", sessionId,
                "message", message,
                "use_llm", useLlm
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, buildHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }

    public String fallbackChat(String sessionId, String message, boolean useLlm, Throwable throwable) {
        log.error("AI service unavailable for chat: {}", throwable.getMessage());
        return "{\"response\":\"El asistente no está disponible temporalmente.\", \"fallback\": true}";
    }
}