package springboot_application.featureflag.stubs;

import springboot_application.featureflag.notification.WebhookNotifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stub implementation of WebhookNotifier for testing.
 * Records all notification calls for verification without HTTP dependency.
 */
public class WebhookNotifierStub extends WebhookNotifier {

    private final List<WebhookNotification> notifications = new ArrayList<>();
    private boolean enabled = true;
    private String webhookUrl = "http://test.example.com/webhook";

    @Override
    public void notifyFlagChange(String eventType, String flagKey, String oldValue, String newValue, String modifiedBy) {
        if (!enabled || webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", eventType);
        payload.put("flagKey", flagKey);
        payload.put("oldValue", oldValue);
        payload.put("newValue", newValue);
        payload.put("modifiedBy", modifiedBy);
        payload.put("timestamp", Instant.now().toString());

        notifications.add(new WebhookNotification(eventType, flagKey, oldValue, newValue, modifiedBy, payload));
    }

    public List<WebhookNotification> getNotifications() {
        return notifications;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void clear() {
        notifications.clear();
    }

    public record WebhookNotification(String eventType, String flagKey, String oldValue,
                                      String newValue, String modifiedBy, Map<String, Object> payload) {}
}
