package robertoCafagna.BE_capstone.repositories.RIDE;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import robertoCafagna.BE_capstone.entities.Route;

import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {

    Page<Route> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId, Pageable pageable);
}
