package robertoCafagna.BE_capstone.repositories.SOCIAL;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.PostMedia;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostMediaRepository
        extends JpaRepository<PostMedia, UUID> {
    List<PostMedia> findByPostIdOrderByOrderIndex(UUID postId);
}
