package robertoCafagna.BE_capstone.DTO.EVENT;

import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventVisibility;
import robertoCafagna.BE_capstone.enums.ParticipationStatus;

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
        boolean locked,
        ParticipationStatus myParticipationStatus,
        boolean organizer
) {
}
