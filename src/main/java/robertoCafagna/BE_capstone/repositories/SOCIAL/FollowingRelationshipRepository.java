package robertoCafagna.BE_capstone.repositories.SOCIAL;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.FollowingRelationship;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowingRelationshipRepository
        extends JpaRepository<FollowingRelationship, UUID> {


    Optional<FollowingRelationship> findByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);

    boolean existsByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);

    Page<FollowingRelationship> findByFollowerId(UUID followerId, Pageable pageable);

    Page<FollowingRelationship> findByFollowedUserId(UUID followedUserId, Pageable pageable);

    long countByFollowerId(UUID followerId);

    long countByFollowedUserId(UUID followedUserId);
}
