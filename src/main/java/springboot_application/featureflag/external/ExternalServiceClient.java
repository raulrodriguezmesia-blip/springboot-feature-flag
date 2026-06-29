package springboot_application.featureflag.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Example external service client that uses CircuitBreaker.
 * This is a placeholder for any external HTTP calls the application might make.
 */
@Service
public class ExternalServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @CircuitBreaker(name = "externalService", fallbackMethod = "fallback")
    public String callExternalService(String url) {
        // In a real application, we would make an HTTP call here
        // For demonstration, we will just return a placeholder
        return restTemplate.getForObject(url, String.class);
    }

    // Fallback method
    public String fallback(String url, Throwable throwable) {
        // Log the error and return a default value or throw a custom exception
        // For now, we will return a default message
        return "Fallback response due to: " + throwable.getMessage();
    }
}