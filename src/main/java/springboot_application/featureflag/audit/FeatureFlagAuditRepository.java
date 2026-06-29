package springboot_application.featureflag.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for FeatureFlagAudit.
 */
@Repository
public interface FeatureFlagAuditRepository extends JpaRepository<FeatureFlagAudit, Long> {
    /**
     * Find audit entries by flag key, ordered by most recent first.
     */
    List<FeatureFlagAudit> findByFlagKeyOrderByModifiedAtDesc(String flagKey);

    /**
     * Find audit entries by flag key with pagination.
     */
    Page<FeatureFlagAudit> findByFlagKeyOrderByModifiedAtDesc(String flagKey, Pageable pageable);

    /**
     * Find audit entries by flag key within date range.
     */
    @Query("SELECT a FROM FeatureFlagAudit a WHERE a.flagKey = :flagKey AND a.modifiedAt BETWEEN :startDate AND :endDate ORDER BY a.modifiedAt DESC")
    List<FeatureFlagAudit> findByFlagKeyAndDateRange(String flagKey, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find audit entries by flag key within date range with pagination.
     */
    @Query("SELECT a FROM FeatureFlagAudit a WHERE a.flagKey = :flagKey AND a.modifiedAt BETWEEN :startDate AND :endDate ORDER BY a.modifiedAt DESC")
    Page<FeatureFlagAudit> findByFlagKeyAndDateRange(String flagKey, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}