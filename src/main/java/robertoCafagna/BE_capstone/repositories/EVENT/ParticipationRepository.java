package robertoCafagna.BE_capstone.repositories.EVENT;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Participation;
import robertoCafagna.BE_capstone.enums.ParticipationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipationRepository
        extends JpaRepository<Participation, UUID> {
    List<Participation> findByEventId(UUID eventId);

    List<Participation> findByUserId(UUID userId);

    boolean existsByEventIdAndUserId(
            UUID eventId,
            UUID userId
    );

    Optional<Participation> findByEventIdAndUserId(UUID eventId, UUID userId);

    List<Participation> findByEventIdAndStatus(UUID eventId, ParticipationStatus status);

    long countByEventIdAndStatus(UUID eventId, ParticipationStatus status);


}
