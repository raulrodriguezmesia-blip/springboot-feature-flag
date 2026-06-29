package springboot_application.featureflag.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collection;

/**
 * Simple in-memory user details service for demo purposes.
 * In a real application, you would fetch users from a database.
 */
@Configuration
public class UserDetailsConfig {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsConfig.class);

    private final PasswordEncoder passwordEncoder;

    public UserDetailsConfig(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            log.info("Loading user: {}", username);

            // For demo purposes, accept any non-empty username
            // In production, you would query your user database
            if (username == null || username.isEmpty()) {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }

            // Default user with ROLE_USER
            String password = passwordEncoder.encode("password"); // Default password
            Collection<org.springframework.security.core.GrantedAuthority> authorities =
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));

            return new org.springframework.security.core.userdetails.User(
                    username,
                    password,
                    true, // enabled
                    true, // accountNonExpired
                    true, // credentialsNonExpired
                    true, // accountNonLocked
                    authorities);
        };
    }
}