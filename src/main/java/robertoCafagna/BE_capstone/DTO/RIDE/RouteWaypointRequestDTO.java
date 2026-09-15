package robertoCafagna.BE_capstone.DTO.RIDE;

import java.util.UUID;

public record RouteWaypointRequestDTO(
        UUID id,
        Double latitude,
        Double longitude,
        String label,
        Integer imageIndex,
        Integer stopMinutes
) {
}
