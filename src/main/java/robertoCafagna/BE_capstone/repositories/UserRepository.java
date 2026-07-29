package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import robertoCafagna.BE_capstone.entities.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByResetPasswordToken(String resetPasswordToken);

}
