package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Ride;

import java.util.List;
import java.util.UUID;

@Repository
public interface RideRepository
        extends JpaRepository<Ride, UUID> {
    List<Ride> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Ride> findByVehicleId(UUID vehicleId);
}
