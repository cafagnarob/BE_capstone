package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Participation;

import java.util.List;
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

}
