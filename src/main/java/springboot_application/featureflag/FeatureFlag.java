package springboot_application.featureflag;

import java.time.LocalDateTime;

public class FeatureFlag {
    private String key;
    private boolean enabled;
    private String description;
    private RolloutStrategy strategy;
    private int rolloutPercentage;
    private String targetingRules;
    private LocalDateTime expiresAt;

    public FeatureFlag() {}

    public FeatureFlag(String key, boolean enabled, String description, RolloutStrategy strategy, int rolloutPercentage, String targetingRules, LocalDateTime expiresAt) {
        this.key = key;
        this.enabled = enabled;
        this.description = description;
        this.strategy = strategy;
        this.rolloutPercentage = rolloutPercentage;
        this.targetingRules = targetingRules;
        this.expiresAt = expiresAt;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RolloutStrategy getStrategy() { return strategy; }
    public void setStrategy(RolloutStrategy strategy) { this.strategy = strategy; }

    public int getRolloutPercentage() { return rolloutPercentage; }
    public void setRolloutPercentage(int rolloutPercentage) { this.rolloutPercentage = rolloutPercentage; }

    public String getTargetingRules() { return targetingRules; }
    public void setTargetingRules(String targetingRules) { this.targetingRules = targetingRules; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}