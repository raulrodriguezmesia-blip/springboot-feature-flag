package springboot_application.featureflag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import springboot_application.featureflag.model.RefreshToken;
import springboot_application.featureflag.repository.RefreshTokenRepository;
import springboot_application.featureflag.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService - Unit Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refreshTokenRepository);
    }

    @Nested
    @DisplayName("createRefreshToken(Long userId)")
    class CreateRefreshTokenByUserId {

        @Test
        @DisplayName("Should create token with valid userId")
        void shouldCreateTokenWithValidUserId() {
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken token = service.createRefreshToken(1L);

            assertNotNull(token);
            assertNotNull(token.getToken());
            assertEquals(1L, token.getUserId());
            assertFalse(token.isRevoked());
            assertNotNull(token.getExpiryDate());
            assertTrue(token.getExpiryDate().isAfter(LocalDateTime.now()));
        }

        @Test
        @DisplayName("Should save token to repository")
        void shouldSaveTokenToRepository() {
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            service.createRefreshToken(42L);

            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Should set expiry date one hour in future")
        void shouldSetExpiryDateOneHourInFuture() {
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            LocalDateTime before = LocalDateTime.now();
            RefreshToken token = service.createRefreshToken(1L);
            LocalDateTime after = LocalDateTime.now().plusHours(1);

            assertFalse(token.getExpiryDate().isBefore(before.plusHours(1).minusMinutes(1)));
            assertFalse(token.getExpiryDate().isAfter(after));
        }
    }

    @Nested
    @DisplayName("createRefreshToken(String username)")
    class CreateRefreshTokenByUsername {

        @Test
        @DisplayName("Should create token from username")
        void shouldCreateTokenFromUsername() {
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken token = service.createRefreshToken("testuser");

            assertNotNull(token);
            assertNotNull(token.getToken());
            assertNotNull(token.getUserId());
        }

        @Test
        @DisplayName("Should generate consistent userId from same username")
        void shouldGenerateConsistentUserIdFromSameUsername() {
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken token1 = service.createRefreshToken("consistent");
            RefreshToken token2 = service.createRefreshToken("consistent");

            assertEquals(token1.getUserId(), token2.getUserId());
        }
    }

    @Nested
    @DisplayName("verifyExpiration")
    class VerifyExpiration {

        @Test
        @DisplayName("Should return token when valid")
        void shouldReturnTokenWhenValid() {
            RefreshToken validToken = RefreshToken.builder()
                    .token("valid-token")
                    .userId(1L)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

            RefreshToken result = service.verifyExpiration("valid-token");

            assertNotNull(result);
            assertEquals("valid-token", result.getToken());
        }

        @Test
        @DisplayName("Should throw when token not found")
        void shouldThrowWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> service.verifyExpiration("unknown"));
            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Should throw when token is revoked")
        void shouldThrowWhenTokenIsRevoked() {
            RefreshToken revokedToken = RefreshToken.builder()
                    .token("revoked-token")
                    .userId(1L)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .revoked(true)
                    .build();

            when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revokedToken));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> service.verifyExpiration("revoked-token"));
            assertTrue(exception.getMessage().contains("expired or revoked"));
        }

        @Test
        @DisplayName("Should throw when token is expired")
        void shouldThrowWhenTokenIsExpired() {
            RefreshToken expiredToken = RefreshToken.builder()
                    .token("expired-token")
                    .userId(1L)
                    .expiryDate(LocalDateTime.now().minusHours(1))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> service.verifyExpiration("expired-token"));
            assertTrue(exception.getMessage().contains("expired or revoked"));
        }
    }

    @Nested
    @DisplayName("revokeToken")
    class RevokeToken {

        @Test
        @DisplayName("Should revoke existing token")
        void shouldRevokeExistingToken() {
            RefreshToken token = RefreshToken.builder()
                    .token("revoke-me")
                    .userId(1L)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findByToken("revoke-me")).thenReturn(Optional.of(token));

            service.revokeToken("revoke-me");

            verify(refreshTokenRepository).save(argThat(RefreshToken::isRevoked));
        }

        @Test
        @DisplayName("Should not throw when token does not exist")
        void shouldNotThrowWhenTokenDoesNotExist() {
            when(refreshTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> service.revokeToken("nonexistent"));
            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteByUserId")
    class DeleteByUserId {

        @Test
        @DisplayName("Should call repository delete")
        void shouldCallRepositoryDelete() {
            service.deleteByUserId(1L);
            verify(refreshTokenRepository).deleteByUserId(1L);
        }
    }

    @Nested
    @DisplayName("cleanupExpiredTokens")
    class CleanupExpiredTokens {

        @Test
        @DisplayName("Should call repository cleanup")
        void shouldCallRepositoryCleanup() {
            service.cleanupExpiredTokens();
            verify(refreshTokenRepository).deleteByExpiryDateBefore(any(LocalDateTime.class));
        }
    }
}
