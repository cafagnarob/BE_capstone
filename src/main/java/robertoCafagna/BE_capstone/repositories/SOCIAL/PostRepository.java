package robertoCafagna.BE_capstone.repositories.SOCIAL;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.route = null WHERE p.route.id = :routeId")
    void clearRouteReference(@Param("routeId") UUID routeId);

    @Query("""
                SELECT p FROM Post p
                WHERE p.user.id = :userId
                   OR p.user.id IN (
                        SELECT f.followedUser.id FROM FollowingRelationship f
                        WHERE f.follower.id = :userId
                   )
                ORDER BY p.createdAt DESC
            """)
    Page<Post> findFollowingFeed(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
                SELECT p FROM Post p
                WHERE p.user.id <> :userId
                  AND p.user.id NOT IN (
                    SELECT f.followedUser.id FROM FollowingRelationship f
                    WHERE f.follower.id = :userId
                  )
                ORDER BY p.createdAt DESC
            """)
    Page<Post> findExploreFeed(@Param("userId") UUID userId, Pageable pageable);

    Page<Post> findByUserIdAndVehicleIdOrderByCreatedAtDesc(UUID userId, UUID vehicleId, Pageable pageable);


    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.vehicle = null WHERE p.vehicle.id IN :vehicleIds")
    void clearVehicleReferences(@Param("vehicleIds") List<UUID> vehicleIds);


    @Query("""
                SELECT p FROM Post p
                WHERE p.user.id <> :userId
                  AND p.user.id NOT IN (
                    SELECT f.followedUser.id FROM FollowingRelationship f
                    WHERE f.follower.id = :userId
                  )
                  AND p.createdAt >= :since
                ORDER BY p.createdAt DESC
            """)
    List<Post> findExploreCandidates(@Param("userId") UUID userId, @Param("since") LocalDateTime since, Pageable pageable);
}
