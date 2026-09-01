package springboot_application.featureflag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import springboot_application.featureflag.notification.WebhookNotifier;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WebhookNotifier - Unit Tests")
class WebhookNotifierTest {

    private WebhookNotifier webhookNotifier;

    @BeforeEach
    void setUp() {
        webhookNotifier = new WebhookNotifier();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("notifyFlagChange - Disabled Webhook")
    class DisabledWebhook {

        @Test
        @DisplayName("Should not throw when webhook is disabled")
        void shouldNotThrowWhenWebhookDisabled() throws Exception {
            setField(webhookNotifier, "enabled", false);
            setField(webhookNotifier, "webhookUrl", "");

            assertDoesNotThrow(() ->
                    webhookNotifier.notifyFlagChange("CREATE", "test.flag", null, "new-value", "admin"));
        }

        @Test
        @DisplayName("Should not throw when webhook URL is blank")
        void shouldNotThrowWhenWebhookUrlIsBlank() throws Exception {
            setField(webhookNotifier, "enabled", true);
            setField(webhookNotifier, "webhookUrl", "");

            assertDoesNotThrow(() ->
                    webhookNotifier.notifyFlagChange("CREATE", "test.flag", null, "new-value", "admin"));
        }

        @Test
        @DisplayName("Should not throw when webhook URL is null")
        void shouldNotThrowWhenWebhookUrlIsNull() throws Exception {
            setField(webhookNotifier, "enabled", true);
            setField(webhookNotifier, "webhookUrl", null);

            assertDoesNotThrow(() ->
                    webhookNotifier.notifyFlagChange("CREATE", "test.flag", null, "new-value", "admin"));
        }
    }

    @Nested
    @DisplayName("notifyFlagChange - Enabled Webhook")
    class EnabledWebhook {

        @Test
        @DisplayName("Should handle connection errors gracefully")
        void shouldHandleConnectionErrorsGracefully() throws Exception {
            setField(webhookNotifier, "enabled", true);
            setField(webhookNotifier, "webhookUrl", "http://localhost:99999/webhook");

            // Should not throw even if the webhook endpoint is unreachable
            assertDoesNotThrow(() ->
                    webhookNotifier.notifyFlagChange("CREATE", "test.flag", null, "{\"key\":\"value\"}", "admin"));
        }

        @Test
        @DisplayName("Should handle all event types without throwing")
        void shouldHandleAllEventTypesWithoutThrowing() throws Exception {
            setField(webhookNotifier, "enabled", true);
            setField(webhookNotifier, "webhookUrl", "http://localhost:99999/webhook");

            assertDoesNotThrow(() ->
                    webhookNotifier.notifyFlagChange("CREATE", "flag.1", null, "val", "admin"));
            assertDoesNotThrow(() ->
                    webhookNotifier.notifyFlagChange("UPDATE", "flag.2", "old", "new", "admin"));
            assertDoesNotThrow(() ->
                    webhookNotifier.notifyFlagChange("DELETE", "flag.3", "old", null, "admin"));
        }
    }
}
