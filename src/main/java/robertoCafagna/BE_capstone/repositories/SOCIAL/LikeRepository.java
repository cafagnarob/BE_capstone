package robertoCafagna.BE_capstone.repositories.SOCIAL;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.Interface.PostLikeCount;
import robertoCafagna.BE_capstone.entities.Like;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {


    void deleteByUserIdAndPostId(
            UUID userId,
            UUID postId
    );


    Optional<Like> findByUserIdAndPostId(UUID userId, UUID postId);

    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    long countByPostId(UUID postId);

    void deleteByPostId(UUID postId);

    @Query("SELECT l.post.id AS postId, COUNT(l) AS count FROM Like l WHERE l.post.id IN :postIds GROUP BY l.post.id")
    List<PostLikeCount> countByPostIdIn(@Param("postIds") List<UUID> postIds);

    @Query("SELECT l.post.id FROM Like l WHERE l.user.id = :userId AND l.post.id IN :postIds")
    List<UUID> findLikedPostIds(@Param("userId") UUID userId, @Param("postIds") List<UUID> postIds);
}
