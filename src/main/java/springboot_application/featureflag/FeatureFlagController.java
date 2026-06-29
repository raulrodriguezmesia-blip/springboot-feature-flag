package springboot_application.featureflag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/feature-flags")
@Validated
@Tag(name = "Feature Flags", description = "Endpoints for managing feature flags")
@SecurityRequirement(name = "bearerAuth")
public class FeatureFlagController {
    private final FeatureFlagService featureFlagService;

    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @Operation(
        summary = "Check if a feature flag is enabled",
        description = "Returns true if the feature flag with the given key is enabled, false otherwise"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature flag status retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "400", description = "Invalid feature flag key",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role",
            content = @Content)
    })
    @GetMapping("/{key}")
    public ResponseEntity<Boolean> isEnabled(
            @Parameter(description = "Feature flag key", required = true)
            @PathVariable @NotBlank(message = "Feature flag key cannot be blank") String key) {
        boolean enabled = featureFlagService.isEnabled(key);
        return ResponseEntity.ok(enabled);
    }

    @Operation(
        summary = "Create or update a feature flag",
        description = "Creates a new feature flag or updates an existing one with the specified key, enabled status, and optional description"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature flag created/updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input (blank key)",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role",
            content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> setEnabled(
            @Parameter(description = "Feature flag key", required = true)
            @RequestParam @NotBlank(message = "Feature flag key cannot be blank") String key,
            @Parameter(description = "Enabled status (true/false)", required = true)
            @RequestParam boolean enabled,
            @Parameter(description = "Optional description of the feature flag")
            @RequestParam(required = false) String description) {
        featureFlagService.setEnabled(key, enabled, description);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Delete a feature flag",
        description = "Deletes the feature flag with the specified key"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Feature flag deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid feature flag key",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role",
            content = @Content)
    })
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteFlag(
            @Parameter(description = "Feature flag key", required = true)
            @PathVariable @NotBlank(message = "Feature flag key cannot be blank") String key) {
        featureFlagService.removeFlag(key);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get all feature flags",
        description = "Returns a map of all feature flags with their keys as values"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature flags retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role",
            content = @Content)
    })
    @GetMapping
    public ResponseEntity<Map<String, FeatureFlag>> getAllFlags() {
        return ResponseEntity.ok(featureFlagService.getAllFlags());
    }
}