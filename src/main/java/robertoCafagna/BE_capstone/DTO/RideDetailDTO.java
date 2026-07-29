package robertoCafagna.BE_capstone.DTO;

import robertoCafagna.BE_capstone.enums.RideType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RideDetailDTO(
        UUID id,
        String title,
        RideType type,
        VehicleSummaryDTO vehicle,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Double distanceKm,
        Double avgSpeedKmH,
        Double maxSpeedKmH,
        int stopsCount,
        int totalStopDurationSeconds,
        String notes,
        boolean inProgress,
        List<RidePointResponseDTO> points
) {
}
