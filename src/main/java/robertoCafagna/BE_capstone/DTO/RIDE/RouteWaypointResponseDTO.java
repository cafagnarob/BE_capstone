package robertoCafagna.BE_capstone.DTO.RIDE;

import java.util.UUID;

public record RouteWaypointResponseDTO(
        UUID id,
        double latitude,
        double longitude,
        int sequence,
        String label,
        String imageUrl,
        Integer stopMinutes
) {
}
