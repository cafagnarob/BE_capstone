package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.FollowingRelationship;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowingRelationshipRepository
        extends JpaRepository<FollowingRelationship, UUID> {


    void deleteByFollowerIdAndFollowedUserId(
            UUID followerId,
            UUID followedUserId
    );


    Optional<FollowingRelationship> findByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);

    boolean existsByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);

    List<FollowingRelationship> findByFollowerId(UUID followerId);

    List<FollowingRelationship> findByFollowedUserId(UUID followedUserId);

    long countByFollowerId(UUID followerId);

    long countByFollowedUserId(UUID followedUserId);

}
