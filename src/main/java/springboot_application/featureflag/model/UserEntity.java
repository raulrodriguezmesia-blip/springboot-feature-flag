package springboot_application.featureflag.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String roles;

    @Column(nullable = false)
    private boolean enabled = true;

    public UserEntity() {}

    public UserEntity(Long id, String username, String password, String roles, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static UserEntityBuilder builder() {
        return new UserEntityBuilder();
    }

    public static class UserEntityBuilder {
        private Long id;
        private String username;
        private String password;
        private String roles;
        private boolean enabled = true;

        public UserEntityBuilder id(Long id) { this.id = id; return this; }
        public UserEntityBuilder username(String username) { this.username = username; return this; }
        public UserEntityBuilder password(String password) { this.password = password; return this; }
        public UserEntityBuilder roles(String roles) { this.roles = roles; return this; }
        public UserEntityBuilder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public UserEntity build() { return new UserEntity(id, username, password, roles, enabled); }
    }
}