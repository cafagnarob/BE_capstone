package robertoCafagna.BE_capstone.DTO.EVENT;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateEventDayRequestDTO(
        String title,
        String description,
        UUID routeId,
        Double meetingPointLat,
        Double meetingPointLng,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer bufferMinutes
) {
}
