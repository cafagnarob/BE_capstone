package robertoCafagna.BE_capstone.DTO;

import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventVisibility;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventDetailDTO(
        UUID id,
        String title,
        String description,
        String organizerUsername,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Double meetingPointLat,
        Double meetingPointLng,
        int maxParticipants,
        long currentParticipants,
        EventVisibility visibility,
        boolean autoApprove,
        EventStatus status,
        LocalDateTime createdAt
) {
}
