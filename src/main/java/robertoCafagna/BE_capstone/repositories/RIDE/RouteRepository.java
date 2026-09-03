package robertoCafagna.BE_capstone.repositories.RIDE;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import robertoCafagna.BE_capstone.entities.Route;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {

    long countByCreatorIdAndCreatedAtAfter(UUID creatorId, LocalDateTime after);

    Page<Route> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId, Pageable pageable);

    @Query("SELECT r FROM Route r LEFT JOIN FETCH r.waypoints WHERE r.id = :id")
    Optional<Route> findByIdWithWaypoints(@Param("id") UUID id);

    Page<Route> findByCreatorIdAndImportableTrueOrderByCreatedAtDesc(UUID creatorId, Pageable pageable);

    boolean existsByIdAndCreatorId(UUID id, UUID creatorId);
}
