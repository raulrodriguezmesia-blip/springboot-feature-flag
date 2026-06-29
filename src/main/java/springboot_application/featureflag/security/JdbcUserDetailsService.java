package springboot_application.featureflag.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import springboot_application.featureflag.model.UserEntity;
import springboot_application.featureflag.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JdbcUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(JdbcUserDetailsService.class);

    private final UserRepository userRepository;

    public JdbcUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("User is disabled: " + username);
        }
        List<GrantedAuthority> authorities = List.of(user.getRoles().split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                authorities
        );
    }
}