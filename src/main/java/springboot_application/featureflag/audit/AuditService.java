package springboot_application.featureflag.audit;

import org.springframework.stereotype.Service;
import springboot_application.featureflag.FeatureFlag;
import springboot_application.featureflag.security.JwtUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Service for auditing feature flag changes.
 */
@Service
public class AuditService {

    private final FeatureFlagAuditRepository auditRepository;
    private final JwtUtils jwtUtils;

    public AuditService(FeatureFlagAuditRepository auditRepository, JwtUtils jwtUtils) {
        this.auditRepository = auditRepository;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Records a change to a feature flag.
     * 
     * @param flagKey The key of the feature flag that changed
     * @param oldValue The previous state of the flag (as a string)
     * @param newValue The new state of the flag (as a string)
     * @param modifiedBy The username of the user who made the change (from JWT)
     */
    public void auditChange(String flagKey, String oldValue, String newValue, String modifiedBy) {
        FeatureFlagAudit audit = new FeatureFlagAudit();
        audit.setFlagKey(flagKey);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setModifiedBy(Objects.requireNonNullElse(modifiedBy, "system"));
        audit.setModifiedAt(LocalDateTime.now());
        auditRepository.save(audit);
    }
}