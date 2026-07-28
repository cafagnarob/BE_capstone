package robertoCafagna.BE_capstone.DTO;

import java.time.LocalDateTime;

public record UpdateEventRequestDTO(
        String title,
        String description,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Double meetingPointLat,
        Double meetingPointLng,
        Integer maxParticipants
) {
}
