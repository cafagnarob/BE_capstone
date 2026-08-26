package robertoCafagna.BE_capstone.repositories.EVENT;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Event;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventVisibility;
import robertoCafagna.BE_capstone.enums.ParticipationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {
    List<Event> findByOrganizerId(UUID userId);

    Page<Event> findByVisibilityAndStatus(EventVisibility visibility, EventStatus status, Pageable pageable);

    Page<Event> findByVisibilityInAndStatus(List<EventVisibility> visibilities, EventStatus status, Pageable pageable);

    boolean existsByRouteId(UUID routeId);

    List<Event> findByStatusAndEndDateTimeBefore(EventStatus status, LocalDateTime dateTime);

    Page<Event> findByOrganizerIdAndParentEventIsNullOrderByStartDateTimeDesc(UUID organizerId, Pageable pageable);

    @Query("""
                SELECT p.event FROM Participation p
                WHERE p.user.id = :userId
                  AND p.status IN :statuses
                  AND p.event.parentEvent IS NULL
                ORDER BY p.event.startDateTime DESC
            """)
    Page<Event> findParticipatingEvents(
            @Param("userId") UUID userId,
            @Param("statuses") List<ParticipationStatus> statuses,
            Pageable pageable
    );
}
