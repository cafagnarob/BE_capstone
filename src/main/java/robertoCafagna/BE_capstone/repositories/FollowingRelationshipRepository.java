package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.FollowingRelationship;

import java.util.UUID;

@Repository
public interface FollowingRelationshipRepository
        extends JpaRepository<FollowingRelationship, UUID> {
}
