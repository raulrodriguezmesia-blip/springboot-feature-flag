package springboot_application.featureflag.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Resilience4j CircuitBreaker.
 */
@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .failureRateThreshold(50)
                .waitDurationInOpenState(java.time.Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(java.io.IOException.class, java.util.concurrent.TimeoutException.class)
                .build();

        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    @Bean
    public io.github.resilience4j.circuitbreaker.CircuitBreaker externalServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("externalService");
    }
}