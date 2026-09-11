package robertoCafagna.BE_capstone.repositories.SOCIAL;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.Interface.CommentLikeCount;
import robertoCafagna.BE_capstone.entities.CommentLike;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    boolean existsByUserIdAndCommentId(UUID userId, UUID commentId);

    void deleteByUserIdAndCommentId(UUID userId, UUID commentId);

    @Query("SELECT cl.comment.id AS commentId, COUNT(cl) AS count FROM CommentLike cl WHERE cl.comment.id IN :commentIds GROUP BY cl.comment.id")
    List<CommentLikeCount> countByCommentIdIn(@Param("commentIds") List<UUID> commentIds);

    @Query("SELECT cl.comment.id FROM CommentLike cl WHERE cl.user.id = :userId AND cl.comment.id IN :commentIds")
    List<UUID> findLikedCommentIds(@Param("userId") UUID userId, @Param("commentIds") List<UUID> commentIds);
}
