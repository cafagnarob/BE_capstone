package robertoCafagna.BE_capstone.DTO.EVENT;

import robertoCafagna.BE_capstone.enums.*;

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
        boolean organizer,
        Double meetingPointLat,
        Double meetingPointLng,
        EventType type,
        Integer tripDurationDays,
        DistanceBucket lockedDistanceBucket
) {
}
