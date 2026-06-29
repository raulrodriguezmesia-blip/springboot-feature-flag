package springboot_application.featureflag.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${app.jwtRotationIntervalMs:3600000}")
    private long jwtRotationIntervalMs;

    private final Map<String, SecretKey> keys = new ConcurrentHashMap<>();
    private volatile String currentKeyId;

    public synchronized String getCurrentKeyId() {
        if (currentKeyId == null) {
            currentKeyId = Base64.getEncoder().encodeToString(jwtSecret.getBytes());
            keys.put(currentKeyId, Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)));
        }
        return currentKeyId;
    }

    public Key key() {
        return keys.getOrDefault(getCurrentKeyId(), Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)));
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Collection<?> authorities = authentication.getAuthorities();
        return generateTokenFromUsername(username, authorities);
    }

    public String generateTokenFromUsername(String username, Collection<?> authorities) {
        String authoritiesString = authorities.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .claim("auth", authoritiesString)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String extractUsername(String token) {
        try {
            return getUserNameFromJwtToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public synchronized void rotateKey() {
        String newKey = UUID.randomUUID().toString();
        String newKeyId = Base64.getEncoder().encodeToString(newKey.getBytes());
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(newKey));
        keys.put(newKeyId, secretKey);
        currentKeyId = newKeyId;
        jwtSecret = newKey;
        log.info("JWT signing key rotated. New keyId={}", newKeyId);
    }
}