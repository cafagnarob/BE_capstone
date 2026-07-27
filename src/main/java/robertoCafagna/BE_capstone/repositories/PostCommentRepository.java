package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.PostComment;

import java.util.UUID;

@Repository
public interface PostCommentRepository
        extends JpaRepository<PostComment, UUID> {

}
