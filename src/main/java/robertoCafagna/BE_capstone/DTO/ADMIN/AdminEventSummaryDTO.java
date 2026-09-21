package robertoCafagna.BE_capstone.DTO.ADMIN;

import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventType;
import robertoCafagna.BE_capstone.enums.EventVisibility;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminEventSummaryDTO(
        UUID id, String title, String organizerUsername,
        LocalDateTime startDateTime, LocalDateTime endDateTime,
        EventStatus status, EventVisibility visibility, EventType type,
        long currentParticipants, int maxParticipants
) {
}
