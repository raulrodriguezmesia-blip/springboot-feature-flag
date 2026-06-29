package springboot_application.featureflag.audit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class FeatureFlagAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String flagKey;
    private String oldValue;
    private String newValue;
    private String modifiedBy;

    @CreationTimestamp
    private LocalDateTime modifiedAt;

    public FeatureFlagAudit() {}

    public FeatureFlagAudit(Long id, String flagKey, String oldValue, String newValue, String modifiedBy, LocalDateTime modifiedAt) {
        this.id = id;
        this.flagKey = flagKey;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = modifiedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFlagKey() { return flagKey; }
    public void setFlagKey(String flagKey) { this.flagKey = flagKey; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }

    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(LocalDateTime modifiedAt) { this.modifiedAt = modifiedAt; }
}