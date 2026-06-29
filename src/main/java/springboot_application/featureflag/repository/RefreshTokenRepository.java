package springboot_application.featureflag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot_application.featureflag.model.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    void deleteByExpiryDateBefore(LocalDateTime expiryDate);
}
