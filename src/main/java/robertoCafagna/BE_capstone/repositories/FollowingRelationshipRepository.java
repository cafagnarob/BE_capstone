package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.FollowingRelationship;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowingRelationshipRepository
        extends JpaRepository<FollowingRelationship, UUID> {

    boolean existsByFollowerIdAndFollowedUserId(
            UUID followerId,
            UUID followedUserId
    );

    void deleteByFollowerIdAndFollowedUserId(
            UUID followerId,
            UUID followedUserId
    );

    List<FollowingRelationship> findByFollowerId(UUID userId);

    List<FollowingRelationship> findByFollowedUserId(UUID userId);

}
