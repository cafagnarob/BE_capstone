package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Ride;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepository
        extends JpaRepository<Ride, UUID> {
    Page<Ride> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Ride> findByVehicleId(UUID vehicleId);

    Optional<Ride> findByUserIdAndEndedAtIsNull(UUID userId);
}
