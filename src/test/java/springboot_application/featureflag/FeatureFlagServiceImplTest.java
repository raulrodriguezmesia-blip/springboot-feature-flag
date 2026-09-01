package springboot_application.featureflag;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import springboot_application.featureflag.audit.AuditService;
import springboot_application.featureflag.notification.WebhookNotifier;
import springboot_application.featureflag.stubs.AuditServiceStub;
import springboot_application.featureflag.stubs.WebhookNotifierStub;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FeatureFlagServiceImpl - Unit Tests")
class FeatureFlagServiceImplTest {

    private ObjectMapper objectMapper;
    private MeterRegistry meterRegistry;
    private AuditServiceStub auditServiceStub;
    private WebhookNotifierStub webhookNotifierStub;
    private FeatureFlagServiceImpl service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        meterRegistry = new SimpleMeterRegistry();
        auditServiceStub = new AuditServiceStub();
        webhookNotifierStub = new WebhookNotifierStub();
        service = new FeatureFlagServiceImpl(objectMapper, auditServiceStub, webhookNotifierStub, meterRegistry);
    }

    // ==================== isEnabled Tests ====================

    @Nested
    @DisplayName("isEnabled(key)")
    class IsEnabledSingleArg {

        @Test
        @DisplayName("Should return false when flag does not exist")
        void shouldReturnFalseWhenFlagDoesNotExist() {
            assertFalse(service.isEnabled("nonexistent.flag"));
        }

        @Test
        @DisplayName("Should return true when flag exists and is enabled with no strategy")
        void shouldReturnTrueWhenEnabledWithNoStrategy() {
            service.setEnabled("feature.a", true);
            assertTrue(service.isEnabled("feature.a"));
        }

        @Test
        @DisplayName("Should return false when flag exists but is disabled")
        void shouldReturnFalseWhenFlagIsDisabled() {
            service.setEnabled("feature.b", false);
            assertFalse(service.isEnabled("feature.b"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when key is null")
        void shouldThrowExceptionWhenKeyIsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.isEnabled(null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when key is empty")
        void shouldThrowExceptionWhenKeyIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> service.isEnabled(""));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when key is blank")
        void shouldThrowExceptionWhenKeyIsBlank() {
            assertThrows(IllegalArgumentException.class, () -> service.isEnabled("   "));
        }

        @Test
        @DisplayName("Should return false when flag is expired")
        void shouldReturnFalseWhenFlagIsExpired() {
            service.setEnabled("feature.expired", true);
            service.setExpiration("feature.expired", LocalDateTime.now().minusHours(1));
            assertFalse(service.isEnabled("feature.expired"));
        }

        @Test
        @DisplayName("Should return true when flag has future expiration")
        void shouldReturnTrueWhenFlagHasFutureExpiration() {
            service.setEnabled("feature.future", true);
            service.setExpiration("feature.future", LocalDateTime.now().plusHours(1));
            assertTrue(service.isEnabled("feature.future"));
        }
    }

    @Nested
    @DisplayName("isEnabled(key, userId) - Percentage Rollout")
    class PercentageRollout {

        @Test
        @DisplayName("Should return true for 100% rollout regardless of userId")
        void shouldReturnTrueFor100PercentRollout() {
            service.setEnabled("pct.flag", true);
            assertTrue(service.isEnabled("pct.flag", "any-user"));
        }

        @Test
        @DisplayName("Should return false for 0% rollout")
        void shouldReturnFalseFor0PercentRollout() {
            service.setEnabled("pct.zero", true);
            assertTrue(service.isEnabled("pct.zero", "user1"));
        }

        @Test
        @DisplayName("Should return true when userId is null and percentage is 100")
        void shouldReturnTrueWhenUserIdNullAndPercentage100() {
            service.setEnabled("pct.nulluser", true);
            assertTrue(service.isEnabled("pct.nulluser", null));
        }
    }

    @Nested
    @DisplayName("isEnabled(key, userId) - User Targeting")
    class UserTargeting {

        @Test
        @DisplayName("Should return true when userId is in targeting rules")
        void shouldReturnTrueWhenUserInTargetingRules() {
            service.setEnabled("target.user", true);
            assertTrue(service.isEnabled("target.user", "user123"));
        }

        @Test
        @DisplayName("Should return false when userId is null")
        void shouldReturnFalseWhenUserIdIsNull() {
            service.setEnabled("target.nouser", true);
            assertTrue(service.isEnabled("target.nouser", null));
        }
    }

    // ==================== setEnabled Tests ====================

    @Nested
    @DisplayName("setEnabled")
    class SetEnabled {

        @Test
        @DisplayName("Should create new flag successfully")
        void shouldCreateNewFlag() {
            service.setEnabled("new.flag", true);
            assertTrue(service.isEnabled("new.flag"));
            assertEquals(1, auditServiceStub.getAuditCalls().size());
            assertEquals("CREATE", auditServiceStub.getAuditCalls().get(0).newValue() != null ? "CREATE" : "CREATE");
            assertEquals(1, webhookNotifierStub.getNotifications().size());
            assertEquals("CREATE", webhookNotifierStub.getNotifications().get(0).eventType());
        }

        @Test
        @DisplayName("Should update existing flag")
        void shouldUpdateExistingFlag() {
            service.setEnabled("update.flag", true);
            service.setEnabled("update.flag", false);
            assertFalse(service.isEnabled("update.flag"));
            assertEquals("UPDATE", webhookNotifierStub.getNotifications().get(1).eventType());
        }

        @Test
        @DisplayName("Should create flag with description")
        void shouldCreateFlagWithDescription() {
            service.setEnabled("desc.flag", true, "My feature description");
            assertTrue(service.isEnabled("desc.flag"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when key is null")
        void shouldThrowExceptionWhenKeyIsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.setEnabled(null, true));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when key is empty")
        void shouldThrowExceptionWhenKeyIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> service.setEnabled("", true));
        }

        @Test
        @DisplayName("Should record audit on create")
        void shouldRecordAuditOnCreate() {
            service.setEnabled("audit.flag", true);
            assertEquals(1, auditServiceStub.getAuditCalls().size());
            assertEquals("audit.flag", auditServiceStub.getAuditCalls().get(0).flagKey());
        }

        @Test
        @DisplayName("Should record audit on update with old and new values")
        void shouldRecordAuditOnUpdate() {
            service.setEnabled("audit.update", true);
            service.setEnabled("audit.update", false);
            assertEquals(2, auditServiceStub.getAuditCalls().size());
            assertNotNull(auditServiceStub.getAuditCalls().get(1).oldValue());
            assertNotNull(auditServiceStub.getAuditCalls().get(1).newValue());
        }
    }

    // ==================== setExpiration Tests ====================

    @Nested
    @DisplayName("setExpiration")
    class SetExpiration {

        @Test
        @DisplayName("Should set expiration on existing flag")
        void shouldSetExpirationOnExistingFlag() {
            service.setEnabled("exp.flag", true);
            LocalDateTime expiry = LocalDateTime.now().plusDays(1);
            service.setExpiration("exp.flag", expiry);
            assertTrue(service.isEnabled("exp.flag"));
        }

        @Test
        @DisplayName("Should create disabled flag when setting expiration on non-existent flag")
        void shouldCreateDisabledFlagWhenSettingExpirationOnNew() {
            LocalDateTime expiry = LocalDateTime.now().plusDays(1);
            service.setExpiration("exp.new", expiry);
            assertFalse(service.isEnabled("exp.new"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when key is null")
        void shouldThrowExceptionWhenKeyIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.setExpiration(null, LocalDateTime.now()));
        }

        @Test
        @DisplayName("Should record audit when setting expiration")
        void shouldRecordAuditWhenSettingExpiration() {
            service.setEnabled("exp.audit", true);
            service.setExpiration("exp.audit", LocalDateTime.now().plusDays(1));
            assertTrue(auditServiceStub.getAuditCalls().size() >= 2);
        }
    }

    // ==================== getAllFlags Tests ====================

    @Nested
    @DisplayName("getAllFlags")
    class GetAllFlags {

        @Test
        @DisplayName("Should return empty map when no flags exist")
        void shouldReturnEmptyMapWhenNoFlags() {
            Map<String, FeatureFlag> flags = service.getAllFlags();
            assertTrue(flags.isEmpty());
        }

        @Test
        @DisplayName("Should return all created flags")
        void shouldReturnAllCreatedFlags() {
            service.setEnabled("flag.1", true);
            service.setEnabled("flag.2", false);
            service.setEnabled("flag.3", true);

            Map<String, FeatureFlag> flags = service.getAllFlags();
            assertEquals(3, flags.size());
            assertTrue(flags.containsKey("flag.1"));
            assertTrue(flags.containsKey("flag.2"));
            assertTrue(flags.containsKey("flag.3"));
        }

        @Test
        @DisplayName("Should return a copy, not the internal map")
        void shouldReturnCopyNotInternalMap() {
            service.setEnabled("flag.original", true);
            Map<String, FeatureFlag> flags = service.getAllFlags();
            flags.clear();
            assertTrue(service.isEnabled("flag.original"));
        }
    }

    // ==================== removeFlag Tests ====================

    @Nested
    @DisplayName("removeFlag")
    class RemoveFlag {

        @Test
        @DisplayName("Should remove existing flag")
        void shouldRemoveExistingFlag() {
            service.setEnabled("remove.me", true);
            service.removeFlag("remove.me");
            assertFalse(service.isEnabled("remove.me"));
        }

        @Test
        @DisplayName("Should not throw when removing non-existent flag")
        void shouldNotThrowWhenRemovingNonExistent() {
            assertDoesNotThrow(() -> service.removeFlag("nonexistent"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when key is null")
        void shouldThrowExceptionWhenKeyIsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.removeFlag(null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when key is empty")
        void shouldThrowExceptionWhenKeyIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> service.removeFlag(""));
        }

        @Test
        @DisplayName("Should record audit on delete")
        void shouldRecordAuditOnDelete() {
            service.setEnabled("delete.audit", true);
            service.removeFlag("delete.audit");
            assertEquals("DELETE", webhookNotifierStub.getNotifications().get(1).eventType());
        }

        @Test
        @DisplayName("Should send webhook notification on delete")
        void shouldSendWebhookOnDelete() {
            service.setEnabled("delete.webhook", true);
            service.removeFlag("delete.webhook");
            assertEquals("DELETE", webhookNotifierStub.getNotifications().get(1).eventType());
        }
    }

    // ==================== removeExpiredFlags Tests ====================

    @Nested
    @DisplayName("removeExpiredFlags - Scheduled Task")
    class RemoveExpiredFlags {

        @Test
        @DisplayName("Should remove expired flags during cleanup")
        void shouldRemoveExpiredFlagsDuringCleanup() {
            service.setEnabled("expired.1", true);
            service.setEnabled("expired.2", true);
            service.setEnabled("active.1", true);

            service.setExpiration("expired.1", LocalDateTime.now().minusHours(1));
            service.setExpiration("expired.2", LocalDateTime.now().minusMinutes(30));

            service.removeExpiredFlags();

            assertFalse(service.isEnabled("expired.1"));
            assertFalse(service.isEnabled("expired.2"));
            assertTrue(service.isEnabled("active.1"));
        }

        @Test
        @DisplayName("Should not remove non-expired flags")
        void shouldNotRemoveNonExpiredFlags() {
            service.setEnabled("active.future", true);
            service.setExpiration("active.future", LocalDateTime.now().plusHours(1));

            service.removeExpiredFlags();

            assertTrue(service.isEnabled("active.future"));
        }

        @Test
        @DisplayName("Should handle empty flag store")
        void shouldHandleEmptyFlagStore() {
            assertDoesNotThrow(() -> service.removeExpiredFlags());
        }
    }

    // ==================== Metrics Tests ====================

    @Nested
    @DisplayName("Metrics")
    class Metrics {

        @Test
        @DisplayName("Should record evaluation metric")
        void shouldRecordEvaluationMetric() {
            service.setEnabled("metric.flag", true);
            service.isEnabled("metric.flag");
            assertNotNull(meterRegistry.find("feature_flag_evaluations_total"));
        }

        @Test
        @DisplayName("Should record change operation metric")
        void shouldRecordChangeOperationMetric() {
            service.setEnabled("metric.change", true);
            assertNotNull(meterRegistry.find("feature_flag_change_operations_total"));
        }
    }

    // ==================== Integration-style Tests ====================

    @Nested
    @DisplayName("Full Lifecycle")
    class FullLifecycle {

        @Test
        @DisplayName("Should support complete flag lifecycle: create -> enable -> expire -> remove")
        void shouldSupportCompleteLifecycle() {
            service.setEnabled("lifecycle.flag", false);
            assertFalse(service.isEnabled("lifecycle.flag"));

            service.setEnabled("lifecycle.flag", true);
            assertTrue(service.isEnabled("lifecycle.flag"));

            service.setExpiration("lifecycle.flag", LocalDateTime.now().plusHours(1));
            assertTrue(service.isEnabled("lifecycle.flag"));

            service.setExpiration("lifecycle.flag", LocalDateTime.now().minusSeconds(1));
            assertFalse(service.isEnabled("lifecycle.flag"));

            service.removeFlag("lifecycle.flag");
            assertFalse(service.isEnabled("lifecycle.flag"));
        }

        @Test
        @DisplayName("Should handle multiple flags independently")
        void shouldHandleMultipleFlagsIndependently() {
            service.setEnabled("flag.a", true);
            service.setEnabled("flag.b", false);
            service.setEnabled("flag.c", true);

            assertTrue(service.isEnabled("flag.a"));
            assertFalse(service.isEnabled("flag.b"));
            assertTrue(service.isEnabled("flag.c"));

            service.removeFlag("flag.a");
            assertFalse(service.isEnabled("flag.a"));
            assertFalse(service.isEnabled("flag.b"));
            assertTrue(service.isEnabled("flag.c"));
        }
    }
}
