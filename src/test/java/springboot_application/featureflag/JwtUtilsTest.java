package springboot_application.featureflag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import springboot_application.featureflag.security.JwtUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtils - Unit Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtils = new JwtUtils();
        setField(jwtUtils, "jwtSecret", "dGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrZXkgZm9yIHRlc3Rpbmc=");
        setField(jwtUtils, "jwtExpirationMs", 3600000);
        setField(jwtUtils, "jwtRotationIntervalMs", 3600000);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Authentication createAuthentication(String username) {
        return new UsernamePasswordAuthenticationToken(
                username, null,
                Collections.singletonList(
                        (org.springframework.security.core.GrantedAuthority) () -> "ROLE_USER"));
    }

    @Nested
    @DisplayName("generateToken(Authentication)")
    class GenerateTokenFromAuthentication {

        @Test
        @DisplayName("Should generate valid JWT from authentication")
        void shouldGenerateValidJwtFromAuthentication() {
            Authentication auth = createAuthentication("testuser");

            String token = jwtUtils.generateToken(auth);

            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertEquals(3, token.split("\\.").length); // Header.Payload.Signature
        }

        @Test
        @DisplayName("Should include username as subject")
        void shouldIncludeUsernameAsSubject() {
            Authentication auth = createAuthentication("admin");

            String token = jwtUtils.generateToken(auth);

            String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);
            assertEquals("admin", extractedUsername);
        }
    }

    @Nested
    @DisplayName("generateTokenFromUsername")
    class GenerateTokenFromUsername {

        @Test
        @DisplayName("Should generate token with username and authorities")
        void shouldGenerateTokenWithUsernameAndAuthorities() {
            @SuppressWarnings("unchecked")
            Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = List.of(
                    (org.springframework.security.core.GrantedAuthority) () -> "ROLE_USER",
                    (org.springframework.security.core.GrantedAuthority) () -> "ROLE_ADMIN"
            );

            String token = jwtUtils.generateTokenFromUsername("testuser", authorities);

            assertNotNull(token);
            assertTrue(jwtUtils.validateJwtToken(token));
        }

        @Test
        @DisplayName("Should generate token with empty authorities")
        void shouldGenerateTokenWithEmptyAuthorities() {
            String token = jwtUtils.generateTokenFromUsername("testuser", Collections.emptyList());

            assertNotNull(token);
            assertTrue(jwtUtils.validateJwtToken(token));
        }
    }

    @Nested
    @DisplayName("validateJwtToken")
    class ValidateJwtToken {

        @Test
        @DisplayName("Should return true for valid token")
        void shouldReturnTrueForValidToken() {
            String token = jwtUtils.generateTokenFromUsername("user", Collections.emptyList());
            assertTrue(jwtUtils.validateJwtToken(token));
        }

        @Test
        @DisplayName("Should return false for invalid token")
        void shouldReturnFalseForInvalidToken() {
            assertFalse(jwtUtils.validateJwtToken("invalid.token.here"));
        }

        @Test
        @DisplayName("Should return false for malformed token")
        void shouldReturnFalseForMalformedToken() {
            assertFalse(jwtUtils.validateJwtToken("not-a-jwt"));
        }

        @Test
        @DisplayName("Should return false for empty token")
        void shouldReturnFalseForEmptyToken() {
            assertFalse(jwtUtils.validateJwtToken(""));
        }

        @Test
        @DisplayName("Should return false for null token")
        void shouldReturnFalseForNullToken() {
            assertFalse(jwtUtils.validateJwtToken(null));
        }
    }

    @Nested
    @DisplayName("extractUsername")
    class ExtractUsername {

        @Test
        @DisplayName("Should extract username from valid token")
        void shouldExtractUsernameFromValidToken() {
            String token = jwtUtils.generateTokenFromUsername("extractuser", Collections.emptyList());
            String username = jwtUtils.extractUsername(token);
            assertEquals("extractuser", username);
        }

        @Test
        @DisplayName("Should return null for invalid token")
        void shouldReturnNullForInvalidToken() {
            String username = jwtUtils.extractUsername("invalid.token");
            assertNull(username);
        }

        @Test
        @DisplayName("Should return null for null token")
        void shouldReturnNullForNullToken() {
            String username = jwtUtils.extractUsername(null);
            assertNull(username);
        }
    }

    @Nested
    @DisplayName("getUserNameFromJwtToken")
    class GetUserNameFromJwtToken {

        @Test
        @DisplayName("Should extract correct username")
        void shouldExtractCorrectUsername() {
            String token = jwtUtils.generateTokenFromUsername("myuser", Collections.emptyList());
            assertEquals("myuser", jwtUtils.getUserNameFromJwtToken(token));
        }

        @Test
        @DisplayName("Should throw exception for tampered token")
        void shouldThrowExceptionForTamperedToken() {
            String token = jwtUtils.generateTokenFromUsername("user", Collections.emptyList());
            String tampered = token.substring(0, token.length() - 2) + "XX";
            assertThrows(Exception.class, () -> jwtUtils.getUserNameFromJwtToken(tampered));
        }
    }

    @Nested
    @DisplayName("Token Expiration")
    class TokenExpiration {

        @Test
        @DisplayName("Should generate valid token with short expiration")
        void shouldGenerateValidTokenWithShortExpiration() throws Exception {
            setField(jwtUtils, "jwtExpirationMs", 5000); // 5 seconds

            String token = jwtUtils.generateTokenFromUsername("user", Collections.emptyList());
            assertTrue(jwtUtils.validateJwtToken(token));
        }
    }

    @Nested
    @DisplayName("Key Rotation")
    class KeyRotation {

        @Test
        @DisplayName("Should rotate key successfully")
        void shouldRotateKeySuccessfully() {
            String tokenBefore = jwtUtils.generateTokenFromUsername("user", Collections.emptyList());

            jwtUtils.rotateKey();

            String tokenAfter = jwtUtils.generateTokenFromUsername("user", Collections.emptyList());
            assertNotNull(tokenAfter);
            assertTrue(jwtUtils.validateJwtToken(tokenAfter));
        }

        @Test
        @DisplayName("Should generate different key ID after rotation")
        void shouldGenerateDifferentKeyIdAfterRotation() {
            String keyIdBefore = jwtUtils.getCurrentKeyId();
            jwtUtils.rotateKey();
            String keyIdAfter = jwtUtils.getCurrentKeyId();

            assertNotEquals(keyIdBefore, keyIdAfter);
        }
    }
}
