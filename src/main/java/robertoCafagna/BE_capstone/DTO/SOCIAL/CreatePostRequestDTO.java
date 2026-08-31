package robertoCafagna.BE_capstone.DTO.SOCIAL;

import java.util.UUID;

public record CreatePostRequestDTO(
        String text,
        UUID eventId,
        UUID rideId,
        UUID vehicleId,
        Boolean includeRoutePhoto,
        UUID routeId
) {
}
