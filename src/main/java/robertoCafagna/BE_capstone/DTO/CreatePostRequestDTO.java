package robertoCafagna.BE_capstone.DTO;

import java.util.UUID;

public record CreatePostRequestDTO(
        String text,
        UUID eventId,
        UUID rideId,
        Boolean includeRoutePhoto
) {
}
