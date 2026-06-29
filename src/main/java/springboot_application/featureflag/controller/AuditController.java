package springboot_application.featureflag.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springboot_application.featureflag.audit.FeatureFlagAudit;
import springboot_application.featureflag.audit.FeatureFlagAuditRepository;

import java.time.LocalDateTime;

/**
 * Controller for retrieving audit history of feature flags.
 */
@RestController
@RequestMapping("/api/feature-flags/audit")
public class AuditController {

    @Autowired
    private FeatureFlagAuditRepository auditRepository;

    /**
     * Get full audit history for a flag key.
     */
    @GetMapping("/{key}")
    public ResponseEntity<Page<FeatureFlagAudit>> getAuditHistory(
            @PathVariable String key,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FeatureFlagAudit> audits = auditRepository.findByFlagKeyOrderByModifiedAtDesc(key, pageable);
        return ResponseEntity.ok(audits);
    }

    /**
     * Get audit history for a flag key within a date range.
     */
    @GetMapping("/{key}/range")
    public ResponseEntity<Page<FeatureFlagAudit>> getAuditHistoryByDateRange(
            @PathVariable String key,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FeatureFlagAudit> audits = auditRepository.findByFlagKeyAndDateRange(
                key, startDate, endDate, pageable);
        return ResponseEntity.ok(audits);
    }
}