package springboot_application.featureflag.stubs;

import springboot_application.featureflag.audit.AuditService;
import springboot_application.featureflag.audit.FeatureFlagAudit;
import springboot_application.featureflag.audit.FeatureFlagAuditRepository;
import springboot_application.featureflag.security.JwtUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stub implementation of AuditService for testing.
 * Records all audit calls for verification without database dependency.
 */
public class AuditServiceStub extends AuditService {

    private final List<AuditCall> auditCalls = new ArrayList<>();

    public AuditServiceStub() {
        super(null, null);
    }

    @Override
    public void auditChange(String flagKey, String oldValue, String newValue, String modifiedBy) {
        auditCalls.add(new AuditCall(flagKey, oldValue, newValue,
                modifiedBy != null ? modifiedBy : "system", LocalDateTime.now()));
    }

    public List<AuditCall> getAuditCalls() {
        return auditCalls;
    }

    public void clear() {
        auditCalls.clear();
    }

    public record AuditCall(String flagKey, String oldValue, String newValue,
                            String modifiedBy, LocalDateTime modifiedAt) {}
}
