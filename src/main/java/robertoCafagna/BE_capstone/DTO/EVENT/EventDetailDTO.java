package robertoCafagna.BE_capstone.DTO.EVENT;

import robertoCafagna.BE_capstone.DTO.RIDE.RouteResponseDTO;
import robertoCafagna.BE_capstone.enums.*;

import java.time.LocalDateTime;
import java.util.List;
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
        String meetingPointAddress,
        int maxParticipants,
        long currentParticipants,
        EventVisibility visibility,
        boolean autoApprove,
        EventStatus status,
        LocalDateTime createdAt,
        RouteResponseDTO route,
        ParticipationStatus myParticipationStatus,
        boolean organizer,
        boolean locked,
        EventType type,
        UUID parentEventId,
        String parentEventTitle,
        List<EventSummaryDTO> children,
        Double totalDistanceMeters,
        AccessRequestStatus myAccessRequestStatus,
        UUID myInviteId
) {
}
