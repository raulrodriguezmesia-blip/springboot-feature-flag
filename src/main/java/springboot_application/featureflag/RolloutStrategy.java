package springboot_application.featureflag;

/**
 * Strategy for rolling out feature flags.
 */
public enum RolloutStrategy {
    /**
     * Roll out to a percentage of users.
     */
    PERCENTAGE,
    /**
     * Target specific users by ID or other attributes.
     */
    USER_TARGETING,
    /**
     * Target segments of users (e.g., by country, device type).
     */
    SEGMENT_TARGETING
}