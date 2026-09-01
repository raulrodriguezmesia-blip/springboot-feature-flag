package springboot_application.featureflag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import springboot_application.featureflag.audit.AuditService;
import springboot_application.featureflag.audit.FeatureFlagAudit;
import springboot_application.featureflag.audit.FeatureFlagAuditRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AuditService - Unit Tests")
class AuditServiceTest {

    private FeatureFlagAuditRepository auditRepository;
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditRepository = mock(FeatureFlagAuditRepository.class);
        // JwtUtils is not used in auditChange, so we pass null
        auditService = new AuditService(auditRepository, null);
        when(auditRepository.save(any(FeatureFlagAudit.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("auditChange")
    class AuditChange {

        @Test
        @DisplayName("Should save audit entry with all fields")
        void shouldSaveAuditEntryWithAllFields() {
            auditService.auditChange("test.flag", "old-value", "new-value", "admin");

            var captor = org.mockito.ArgumentCaptor.forClass(FeatureFlagAudit.class);
            verify(auditRepository).save(captor.capture());

            FeatureFlagAudit saved = captor.getValue();
            assertEquals("test.flag", saved.getFlagKey());
            assertEquals("old-value", saved.getOldValue());
            assertEquals("new-value", saved.getNewValue());
            assertEquals("admin", saved.getModifiedBy());
            assertNotNull(saved.getModifiedAt());
        }

        @Test
        @DisplayName("Should use 'system' when modifiedBy is null")
        void shouldUseSystemWhenModifiedByIsNull() {
            auditService.auditChange("test.flag", "old", "new", null);

            var captor = org.mockito.ArgumentCaptor.forClass(FeatureFlagAudit.class);
            verify(auditRepository).save(captor.capture());

            assertEquals("system", captor.getValue().getModifiedBy());
        }

        @Test
        @DisplayName("Should handle null oldValue (CREATE operation)")
        void shouldHandleNullOldValue() {
            auditService.auditChange("new.flag", null, "new-value", "admin");

            var captor = org.mockito.ArgumentCaptor.forClass(FeatureFlagAudit.class);
            verify(auditRepository).save(captor.capture());

            assertNull(captor.getValue().getOldValue());
            assertEquals("new-value", captor.getValue().getNewValue());
        }

        @Test
        @DisplayName("Should handle null newValue (DELETE operation)")
        void shouldHandleNullNewValue() {
            auditService.auditChange("delete.flag", "old-value", null, "admin");

            var captor = org.mockito.ArgumentCaptor.forClass(FeatureFlagAudit.class);
            verify(auditRepository).save(captor.capture());

            assertEquals("old-value", captor.getValue().getOldValue());
            assertNull(captor.getValue().getNewValue());
        }

        @Test
        @DisplayName("Should set current timestamp")
        void shouldSetCurrentTimestamp() {
            LocalDateTime before = LocalDateTime.now();
            auditService.auditChange("test.flag", "old", "new", "admin");
            LocalDateTime after = LocalDateTime.now();

            var captor = org.mockito.ArgumentCaptor.forClass(FeatureFlagAudit.class);
            verify(auditRepository).save(captor.capture());

            assertFalse(captor.getValue().getModifiedAt().isBefore(before));
            assertFalse(captor.getValue().getModifiedAt().isAfter(after));
        }
    }
}
