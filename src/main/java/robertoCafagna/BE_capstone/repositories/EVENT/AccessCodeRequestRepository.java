package robertoCafagna.BE_capstone.repositories.EVENT;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.AccessCodeRequest;
import robertoCafagna.BE_capstone.enums.AccessRequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessCodeRequestRepository extends JpaRepository<AccessCodeRequest, UUID> {
    Optional<AccessCodeRequest> findByEventIdAndRequesterId(UUID eventId, UUID requesterId);

    List<AccessCodeRequest> findByEventIdAndStatus(UUID eventId, AccessRequestStatus status);
}