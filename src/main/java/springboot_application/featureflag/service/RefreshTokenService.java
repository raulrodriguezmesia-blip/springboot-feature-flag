package springboot_application.featureflag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot_application.featureflag.model.RefreshToken;
import springboot_application.featureflag.repository.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        return createRefreshTokenForUser(userId);
    }

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        // Para demo, usamos hashcode como ID de usuario
        Long userId = (long) username.hashCode();
        return createRefreshTokenForUser(userId);
    }

    private RefreshToken createRefreshTokenForUser(Long userId) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUserId(userId);
        refreshToken.setExpiryDate(expiryDate);
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for userId={}, expires={}", userId, expiryDate);
        return refreshToken;
    }

    @Transactional
    public RefreshToken verifyExpiration(String token) {
        Optional<RefreshToken> opt = refreshTokenRepository.findByToken(token);
        RefreshToken refreshToken = opt.orElseThrow(() -> new RuntimeException("Refresh token not found"));
        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }
        return refreshToken;
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}