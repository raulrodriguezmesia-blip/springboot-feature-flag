package springboot_application.featureflag.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Sends notifications via HTTP webhook when feature flags change.
 */
@Service
public class WebhookNotifier {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notification.webhook.url:}")
    private String webhookUrl;

    @Value("${notification.webhook.enabled:false}")
    private boolean enabled;

    /**
     * Sends a notification about a feature flag change.
     * 
     * @param eventType The type of event (CREATE, UPDATE, DELETE)
     * @param flagKey The key of the feature flag
     * @param oldValue The previous state of the flag (null for CREATE)
     * @param newValue The new state of the flag (null for DELETE)
     * @param modifiedBy The user who made the change
     */
    public void notifyFlagChange(String eventType, String flagKey, String oldValue, String newValue, String modifiedBy) {
        if (!enabled || webhookUrl.isBlank()) {
            // Webhook is not configured or disabled
            return;
        }

        // Create the payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", eventType);
        payload.put("flagKey", flagKey);
        payload.put("oldValue", oldValue);
        payload.put("newValue", newValue);
        payload.put("modifiedBy", modifiedBy);
        payload.put("timestamp", Instant.now().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            // We could log the response if needed
        } catch (Exception e) {
            // In a production system, we might want to retry or use a dead letter queue
            // For now, we will just log the error (but we do not have a logger here)
            // We will rely on the caller to handle the exception or we can swallow it
            // to avoid breaking the main operation.
            // Since we are in a notification service, we do not want to fail the main request.
            // So we will just swallow the exception.
        }
    }
}