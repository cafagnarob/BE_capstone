package robertoCafagna.BE_capstone.services.RIDE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.GARAGE.VehicleSummaryDTO;
import robertoCafagna.BE_capstone.DTO.RIDE.*;
import robertoCafagna.BE_capstone.entities.Ride;
import robertoCafagna.BE_capstone.entities.RidePoint;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.entities.Vehicle;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.ForbiddenException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.GARAGE.VehicleRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RidePointRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RideRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {

    private final RideRepository rideRepository;
    private final RidePointRepository ridePointRepository;
    private final VehicleRepository vehicleRepository;


    @Transactional
    public RideSummaryDTO startRide(User currentUser, StartRideRequestDTO body) {
        if (rideRepository.findByUserIdAndEndedAtIsNull(currentUser.getId()).isPresent()) {
            throw new BadRequestException("Hai già un giro in corso, terminalo prima di iniziarne uno nuovo");
        }

        Vehicle vehicle = null;
        if (body.vehicleId() != null) {
            vehicle = vehicleRepository.findByIdAndUserId(body.vehicleId(), currentUser.getId())
                    .orElseThrow(() -> new NotFoundException("Veicolo non trovato nel tuo garage"));
        }

        Ride ride = new Ride(currentUser, vehicle, body.title());
        if (body.type() != null) ride.setType(body.type());

        rideRepository.save(ride);
        log.info("Utente {} ha iniziato un giro ({})", currentUser.getId(), ride.getId());
        return toSummaryDTO(ride);
    }


    @Transactional
    public RideDetailDTO finishRide(User currentUser, UUID rideId, FinishRideRequestDTO body) {
        Ride ride = getOwnedInProgressRide(currentUser, rideId);

        if (body.endedAt().isBefore(ride.getStartedAt())) {
            throw new BadRequestException("L'orario di fine non può precedere quello di inizio");
        }

        ride.finishRide(
                body.endedAt(), body.distanceKm(), body.avgSpeedKmH(), body.maxSpeedKmH(),
                body.stopsCount(), body.totalStopDurationSeconds()
        );
        rideRepository.save(ride);

        List<RidePoint> points = body.points().stream()
                .map(p -> new RidePoint(
                        ride, p.latitude(), p.longitude(), p.sequence(),
                        p.speedKmh(), p.altitude(), p.recordedAt()
                ))
                .toList();
        ridePointRepository.saveAll(points);

        log.info("Utente {} ha terminato il giro {} ({} punti GPS)",
                currentUser.getId(), rideId, points.size());
        return toDetailDTO(ride, points.stream().map(this::toPointDTO).toList());
    }

    public Page<RideSummaryDTO> getUserRides(User currentUser, UUID vehicleId, int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size);

        Page<Ride> rides = vehicleId != null
                ? rideRepository.findByUserIdAndVehicleIdOrderByCreatedAtDesc(currentUser.getId(), vehicleId, pageable)
                : rideRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);

        return rides.map(this::toSummaryDTO);
    }

    public RideDetailDTO getRideById(User currentUser, UUID rideId) {
        Ride ride = getOwnedRide(currentUser, rideId);
        List<RidePointResponseDTO> points = ridePointRepository.findByRideIdOrderBySequence(rideId)
                .stream().map(this::toPointDTO).toList();
        return toDetailDTO(ride, points);
    }

    @Transactional
    public void deleteRide(User currentUser, UUID rideId) {
        Ride ride = getOwnedRide(currentUser, rideId);
        rideRepository.delete(ride);
        log.info("Utente {} ha eliminato il giro {}", currentUser.getId(), rideId);
    }


    private Ride getOwnedRide(User currentUser, UUID rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new NotFoundException("Giro non trovato"));
        if (!ride.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Non sei il proprietario di questo giro");
        }
        return ride;
    }

    private Ride getOwnedInProgressRide(User currentUser, UUID rideId) {
        Ride ride = getOwnedRide(currentUser, rideId);
        if (ride.getEndedAt() != null) {
            throw new BadRequestException("Questo giro è già stato concluso");
        }
        return ride;
    }

    private RideSummaryDTO toSummaryDTO(Ride ride) {
        return new RideSummaryDTO(
                ride.getId(), ride.getTitle(), ride.getType(),
                toVehicleSummary(ride.getVehicle()),
                ride.getStartedAt(), ride.getEndedAt(), ride.getDistanceKm(), ride.getAvgSpeedKmH(),
                ride.getEndedAt() == null
        );
    }

    private RideDetailDTO toDetailDTO(Ride ride, List<RidePointResponseDTO> points) {
        return new RideDetailDTO(
                ride.getId(), ride.getTitle(), ride.getType(),
                toVehicleSummary(ride.getVehicle()),
                ride.getStartedAt(), ride.getEndedAt(), ride.getDistanceKm(),
                ride.getAvgSpeedKmH(), ride.getMaxSpeedKmH(), ride.getStopsCount(),
                ride.getTotalStopDurationSeconds(), ride.getNotes(),
                ride.getEndedAt() == null, points
        );
    }

    private RidePointResponseDTO toPointDTO(RidePoint point) {
        return new RidePointResponseDTO(
                point.getLatitude(), point.getLongitude(), point.getSequence(),
                point.getSpeedKmh(), point.getAltitude()
        );
    }

    private VehicleSummaryDTO toVehicleSummary(Vehicle vehicle) {
        if (vehicle == null) return null;
        return new VehicleSummaryDTO(vehicle.getId(), vehicle.getNickname(), vehicle.getPhotoUrl(), vehicle.getModel().getBrand().getName(),
                vehicle.getModel().getName());
    }


}
