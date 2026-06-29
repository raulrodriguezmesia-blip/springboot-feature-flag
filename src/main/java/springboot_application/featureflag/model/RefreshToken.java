package springboot_application.featureflag.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;

    public RefreshToken() {}

    public RefreshToken(Long id, String token, Long userId, LocalDateTime expiryDate, boolean revoked) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public static RefreshTokenBuilder builder() {
        return new RefreshTokenBuilder();
    }

    public static class RefreshTokenBuilder {
        private Long id;
        private String token;
        private Long userId;
        private LocalDateTime expiryDate;
        private boolean revoked = false;

        public RefreshTokenBuilder id(Long id) { this.id = id; return this; }
        public RefreshTokenBuilder token(String token) { this.token = token; return this; }
        public RefreshTokenBuilder userId(Long userId) { this.userId = userId; return this; }
        public RefreshTokenBuilder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public RefreshTokenBuilder revoked(boolean revoked) { this.revoked = revoked; return this; }
        public RefreshToken build() { return new RefreshToken(id, token, userId, expiryDate, revoked); }
    }
}