package robertoCafagna.BE_capstone.DTO.EVENT;

import robertoCafagna.BE_capstone.enums.InviteStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventInviteResponseDTO(
        UUID id,
        UUID eventId,
        String eventTitle,
        String invitedUsername,
        InviteStatus status,
        LocalDateTime createdAt
) {
}
