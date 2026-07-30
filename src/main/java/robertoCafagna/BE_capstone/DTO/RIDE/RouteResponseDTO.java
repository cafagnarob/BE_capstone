package robertoCafagna.BE_capstone.DTO.RIDE;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RouteResponseDTO(
        UUID id,
        String name,
        List<RouteWaypointResponseDTO> waypoints,
        String encodedPolyline,
        double distanceMeters,
        double durationSeconds,
        boolean avoidHighways,
        boolean avoidTolls,
        boolean avoidFerries,
        String googleMapsUrl,
        LocalDateTime createdAt
) {
}
