package robertoCafagna.BE_capstone.repositories.RIDE;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.Interface.RideTrendPoint;
import robertoCafagna.BE_capstone.entities.Ride;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepository
        extends JpaRepository<Ride, UUID> {
    Page<Ride> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Ride> findByVehicleId(UUID vehicleId);

    Page<Ride> findByUserIdAndVehicleIdOrderByCreatedAtDesc(UUID userId, UUID vehicleId, Pageable pageable);

    Optional<Ride> findByUserIdAndEndedAtIsNull(UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(r.distanceKm), 0) FROM Ride r")
    double sumTotalDistanceKm();

    @Query("SELECT r.createdAt AS createdAt, r.distanceKm AS distanceKm FROM Ride r WHERE r.createdAt >= :since")
    List<RideTrendPoint> findTrendDataSince(@Param("since") LocalDateTime since);
}
