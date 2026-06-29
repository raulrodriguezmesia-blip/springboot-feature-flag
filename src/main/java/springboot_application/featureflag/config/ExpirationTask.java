package springboot_application.featureflag.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springboot_application.featureflag.FeatureFlag;
import springboot_application.featureflag.FeatureFlagService;
import springboot_application.featureflag.audit.AuditService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Scheduled task to clean up expired feature flags.
 */
@Component
public class ExpirationTask {

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private AuditService auditService;

    /**
     * Runs every minute to check for and remove expired flags.
     */
    @Scheduled(fixedRate = 60000) // 60 seconds
    public void removeExpiredFlags() {
        Map<String, FeatureFlag> allFlags = featureFlagService.getAllFlags();
        for (Map.Entry<String, FeatureFlag> entry : allFlags.entrySet()) {
            String key = entry.getKey();
            FeatureFlag flag = entry.getValue();
            if (flag.getExpiresAt() != null && flag.getExpiresAt().isBefore(LocalDateTime.now())) {
                // Flag is expired, remove it
                String username = "system"; // scheduled task runs as system
                // Get current value for auditing before removal
                String oldValue = null;
                try {
                    // We'll serialize the flag to JSON for auditing
                    // Since we don't have easy access to ObjectMapper here, we could call a method on service,
                    // but to keep it simple, we'll just note that the flag was removed due to expiration.
                    // We'll store a simple string indicating expiration.
                    oldValue = "Expired flag: " + flag;
                } catch (Exception e) {
                    oldValue = "Expired flag (unable to serialize)";
                }
                featureFlagService.removeFlag(key);
                auditService.auditChange(key, oldValue, null, username);
                // Note: We could also send a webhook notification here if desired.
            }
        }
    }
}