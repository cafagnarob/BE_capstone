package robertoCafagna.BE_capstone.repositories.GARAGE;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository
        extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findByUserId(UUID userId);

    Optional<Vehicle> findByIdAndUserId(
            UUID vehicleId,
            UUID userId
    );

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByUserIdAndModelId(UUID userId, UUID modelId);


}
