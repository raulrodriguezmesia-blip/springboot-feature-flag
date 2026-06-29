package springboot_application.featureflag.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import springboot_application.featureflag.model.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
