package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Event;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventVisibility;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByOrganizerId(UUID userId);

    Page<Event> findByVisibilityAndStatus(EventVisibility visibility, EventStatus status, Pageable pageable);

    Page<Event> findByVisibilityInAndStatus(List<EventVisibility> visibilities, EventStatus status, Pageable pageable);
}
