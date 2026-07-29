package robertoCafagna.BE_capstone.DTO;

import robertoCafagna.BE_capstone.enums.RideType;

import java.time.LocalDateTime;
import java.util.UUID;

public record RideSummaryDTO(
        UUID id,
        String title,
        RideType type,
        VehicleSummaryDTO vehicle,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Double distanceKm,
        Double avgSpeedKmH,
        boolean inProgress
) {
}
