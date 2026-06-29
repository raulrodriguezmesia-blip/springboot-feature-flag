package springboot_application.featureflag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import springboot_application.featureflag.audit.AuditService;
import springboot_application.featureflag.notification.WebhookNotifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FeatureFlagServiceImpl implements FeatureFlagService {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagServiceImpl.class);
    private static final String CACHE_NAME = "featureFlags";

    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final WebhookNotifier webhookNotifier;
    private final MeterRegistry meterRegistry;
    private final Map<String, FeatureFlag> inMemoryFlags = new ConcurrentHashMap<>();

    public FeatureFlagServiceImpl(ObjectMapper objectMapper,
                                   AuditService auditService,
                                   WebhookNotifier webhookNotifier,
                                   MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;
        this.auditService = auditService;
        this.webhookNotifier = webhookNotifier;
        this.meterRegistry = meterRegistry;
    }

    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#key")
    public boolean isEnabled(String key) {
        return isEnabled(key, null);
    }

    @Override
    public boolean isEnabled(String key, String userId) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("Feature flag key cannot be null or empty");
        }

        boolean result = false;

        try {
            FeatureFlag flag = inMemoryFlags.get(key);
            if (flag == null) {
                return false;
            }
            if (isExpired(flag)) {
                return false;
            }
            result = evaluateFlag(flag, userId);
            return result;
        } catch (Exception e) {
            log.error("Error checking feature flag {}: {}", key, e.getMessage());
            return false;
        } finally {
            Counter.builder("feature_flag_evaluations_total")
                    .tags("flag", key, "result", String.valueOf(result))
                    .register(meterRegistry)
                    .increment();
        }
    }

    private void putFlag(String key, FeatureFlag flag) {
        inMemoryFlags.put(key, flag);
    }

    private boolean isExpired(FeatureFlag flag) {
        return flag.getExpiresAt() != null && flag.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private boolean evaluateFlag(FeatureFlag flag, String userId) {
        if (!flag.isEnabled()) {
            return false;
        }
        if (flag.getStrategy() == null) {
            return true;
        }
        switch (flag.getStrategy()) {
            case PERCENTAGE: return evaluatePercentageRollout(flag, userId);
            case USER_TARGETING: return evaluateUserTargeting(flag, userId);
            case SEGMENT_TARGETING: return evaluateSegmentTargeting(flag, userId);
            default: return false;
        }
    }

    private boolean evaluatePercentageRollout(FeatureFlag flag, String userId) {
        if (!StringUtils.hasText(userId)) return flag.getRolloutPercentage() == 100;
        int hash = Math.abs(userId.hashCode());
        int bucket = hash % 100;
        return bucket < flag.getRolloutPercentage();
    }

    private boolean evaluateUserTargeting(FeatureFlag flag, String userId) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(flag.getTargetingRules())) return false;
        String[] allowedUsers = flag.getTargetingRules().split(",");
        for (String allowedUser : allowedUsers) if (allowedUser.trim().equals(userId)) return true;
        return false;
    }

    private boolean evaluateSegmentTargeting(FeatureFlag flag, String userId) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(flag.getTargetingRules())) return false;
        return true;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return "system";
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails)
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        else if (principal instanceof String) return (String) principal;
        else return principal.toString();
    }

    @Override
    @CachePut(cacheNames = CACHE_NAME, key = "#key")
    public void setEnabled(String key, boolean enabled) {
        setEnabled(key, enabled, null);
    }

    @Override
    @CachePut(cacheNames = CACHE_NAME, key = "#key")
    public void setEnabled(String key, boolean enabled, String description) {
        if (!StringUtils.hasText(key))
            throw new IllegalArgumentException("Feature flag key cannot be null or empty");

        String username = getCurrentUsername();
        String oldValueJson = null;
        FeatureFlag oldFlag = inMemoryFlags.get(key);
        if (oldFlag != null) {
            try { oldValueJson = objectMapper.writeValueAsString(oldFlag); }
            catch (JsonProcessingException e) { }
        }

        FeatureFlag newFlag = new FeatureFlag(key, enabled, description, null, 0, null, null);
        if (oldFlag != null && oldFlag.getExpiresAt() != null) newFlag.setExpiresAt(oldFlag.getExpiresAt());

        putFlag(key, newFlag);

        String operation = (oldFlag == null) ? "CREATE" : "UPDATE";
        Counter.builder("feature_flag_change_operations_total")
                .tags("flag", key, "operation", operation)
                .register(meterRegistry)
                .increment();

        String newValueJson = null;
        try { newValueJson = objectMapper.writeValueAsString(newFlag); }
        catch (JsonProcessingException e) { }

        auditService.auditChange(key, oldValueJson, newValueJson, username);
        webhookNotifier.notifyFlagChange(operation, key, oldValueJson, newValueJson, username);
    }

    @Override
    public void setExpiration(String key, LocalDateTime expiresAt) {
        if (!StringUtils.hasText(key))
            throw new IllegalArgumentException("Feature flag key cannot be null or empty");
        String username = getCurrentUsername();
        FeatureFlag oldFlag = inMemoryFlags.get(key);
        String oldValueJson = null;
        if (oldFlag != null) {
            try { oldValueJson = objectMapper.writeValueAsString(oldFlag); }
            catch (JsonProcessingException e) { }
        }
        FeatureFlag newFlag = (oldFlag != null) ? new FeatureFlag(
                oldFlag.getKey(), oldFlag.isEnabled(), oldFlag.getDescription(),
                oldFlag.getStrategy(), oldFlag.getRolloutPercentage(), oldFlag.getTargetingRules(), expiresAt)
            : new FeatureFlag(key, false, null, null, 0, null, expiresAt);
        putFlag(key, newFlag);
        String newValueJson = null;
        try { newValueJson = objectMapper.writeValueAsString(newFlag); }
        catch (JsonProcessingException e) { }
        auditService.auditChange(key, oldValueJson, newValueJson, username);
        webhookNotifier.notifyFlagChange((oldFlag == null)? "CREATE" : "UPDATE", key, oldValueJson, newValueJson, username);
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public Map<String, FeatureFlag> getAllFlags() {
        return new HashMap<>(inMemoryFlags);
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public void removeFlag(String key) {
        if (!StringUtils.hasText(key))
            throw new IllegalArgumentException("Feature flag key cannot be null or empty");
        String username = getCurrentUsername();
        FeatureFlag oldFlag = inMemoryFlags.remove(key);
        String oldValueJson = null;
        if (oldFlag != null) {
            try { oldValueJson = objectMapper.writeValueAsString(oldFlag); }
            catch (JsonProcessingException e) { }
        }

        Counter.builder("feature_flag_change_operations_total")
                .tags("flag", key, "operation", "DELETE")
                .register(meterRegistry)
                .increment();

        auditService.auditChange(key, oldValueJson, null, username);
        webhookNotifier.notifyFlagChange("DELETE", key, oldValueJson, null, username);
    }

    @Scheduled(fixedRateString = "${app.featureFlag.expirationCheckIntervalMs:60000}")
    public void removeExpiredFlags() {
        log.info("Starting scheduled cleanup of expired feature flags");
        int removedCount = 0;
        for (Map.Entry<String, FeatureFlag> entry : inMemoryFlags.entrySet()) {
            String key = entry.getKey();
            FeatureFlag flag = entry.getValue();
            if (flag != null && isExpired(flag)) { removeFlag(key); removedCount++; }
        }
        log.info("Completed scheduled cleanup. Removed {} expired flags", removedCount);
    }
}