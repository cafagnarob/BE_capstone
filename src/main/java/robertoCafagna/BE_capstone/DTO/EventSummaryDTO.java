package robertoCafagna.BE_capstone.DTO;

import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventVisibility;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventSummaryDTO(
        UUID id,
        String title,
        String organizerUsername,
        LocalDateTime startDateTime,
        int maxParticipants,
        long currentParticipants,
        EventVisibility visibility,
        EventStatus status,
        boolean locked
) {
}
