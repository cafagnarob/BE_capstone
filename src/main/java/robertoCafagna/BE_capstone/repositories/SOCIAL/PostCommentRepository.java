package robertoCafagna.BE_capstone.repositories.SOCIAL;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.Interface.PostCommentCount;
import robertoCafagna.BE_capstone.entities.PostComment;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    Page<PostComment> findByPostIdOrderByCreatedAtAsc(UUID postId, Pageable pageable);

    long countByPostId(UUID postId);

    void deleteByPostId(UUID postId);

    @Query("SELECT c.post.id AS postId, COUNT(c) AS count FROM PostComment c WHERE c.post.id IN :postIds GROUP BY c.post.id")
    List<PostCommentCount> countByPostIdIn(@Param("postIds") List<UUID> postIds);
}
