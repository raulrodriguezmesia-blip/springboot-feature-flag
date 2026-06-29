package springboot_application.featureflag;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for managing feature flags.
 */
public interface FeatureFlagService {
    boolean isEnabled(String key);
    boolean isEnabled(String key, String userId);
    void setEnabled(String key, boolean enabled);
    void setEnabled(String key, boolean enabled, String description);
    void setExpiration(String key, LocalDateTime expiresAt);
    Map<String, FeatureFlag> getAllFlags();
    void removeFlag(String key);
}