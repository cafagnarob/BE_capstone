package robertoCafagna.BE_capstone.repositories.USER;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import robertoCafagna.BE_capstone.entities.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    Optional<User> findByEmailVerificationToken(String token);

    @Query("SELECT u FROM User u WHERE u.active = true AND (" +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.surname) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchActive(@Param("query") String query, Pageable pageable);

    List<User> findByActiveTrue();


    long countByActiveTrue();

    long countByCreatedAtAfter(LocalDateTime since);

    @Query("SELECT u.createdAt FROM User u WHERE u.createdAt >= :since")
    List<LocalDateTime> findCreatedAtSince(@Param("since") LocalDateTime since);


    long countByLastLoginAfter(LocalDateTime since);

    long countByLastLoginIsNull();

    long countByEmailVerifiedFalse();

    long countByActiveFalseAndDeletedAtIsNull();

    long countByDeletedAtIsNotNull();

}
