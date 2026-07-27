package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Like;

import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    boolean existsByUserIdAndPostId(
            UUID userId,
            UUID postId
    );

    void deleteByUserIdAndPostId(
            UUID userId,
            UUID postId
    );

    long countByPostId(UUID postId);
}
