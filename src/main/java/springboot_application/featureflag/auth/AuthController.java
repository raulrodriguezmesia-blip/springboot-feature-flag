package springboot_application.featureflag.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import springboot_application.featureflag.security.JwtUtils;
import springboot_application.featureflag.service.RefreshTokenService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and token management")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                          PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Operation(summary = "Authenticate user and generate JWT token", description = "Validates user credentials and returns a JWT token for subsequent authenticated requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or missing fields", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid username or password", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateToken(authentication);
            var refreshToken = refreshTokenService.createRefreshToken(authentication.getName());

            return ResponseEntity.ok(new AuthResponse(jwt, refreshToken.getToken()));
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid username or password"));
        }
    }

    @Operation(summary = "Refresh access token using a valid refresh token", description = "Returns a new JWT access token if the refresh token is still valid")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            refreshTokenService.verifyExpiration(request.getRefreshToken());
            String username = jwtUtils.extractUsername(request.getRefreshToken());
            if (username == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid refresh token"));
            }
            String newJwt = jwtUtils.generateTokenFromUsername(username, java.util.Collections.emptyList());
            return ResponseEntity.ok(new AuthResponse(newJwt, request.getRefreshToken()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Refresh token invalid or expired: " + e.getMessage()));
        }
    }

    @Operation(summary = "Logout user by invalidating refresh tokens", description = "Revokes all refresh tokens for the current authenticated user")
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Logout requested for user: {}", username);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    public static class LoginRequest {
        @Parameter(description = "Username for authentication", required = true)
        @Schema(example = "admin")
        private String username;

        @Parameter(description = "Password for authentication", required = true)
        @Schema(example = "password123")
        private String password;

        public LoginRequest() {}

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RefreshTokenRequest {
        @Parameter(description = "Valid refresh token", required = true)
        @Schema(example = "c2f5...")
        private String refreshToken;

        public RefreshTokenRequest() {}

        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class AuthResponse {
        @Parameter(description = "JWT access token", required = true)
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String accessToken;

        @Parameter(description = "Refresh token", required = true)
        @Schema(example = "d8a7...")
        private String refreshToken;

        public AuthResponse() {}

        public AuthResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class MessageResponse {
        private String message;

        public MessageResponse() {}

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}