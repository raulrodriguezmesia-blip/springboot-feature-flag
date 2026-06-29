package springboot_application.featureflag.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot_application.featureflag.FeatureFlagService;

import java.util.Map;

/**
 * Simple admin dashboard for monitoring feature flags.
 * In a real application, this would be backed by a UI.
 */
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private FeatureFlagService featureFlagService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        Map<String, Boolean> flagStatus = featureFlagService.getAllFlags().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().isEnabled()
                ));

        return ResponseEntity.ok(java.util.Map.of(
                "totalFlags", flagStatus.size(),
                "enabledFlags", flagStatus.values().stream().filter(Boolean::booleanValue).count(),
                "disabledFlags", flagStatus.values().stream().filter(b -> !b).count(),
                "flagStatus", flagStatus
        ));
    }
}