package robertoCafagna.BE_capstone.DTO.EVENT;

import robertoCafagna.BE_capstone.enums.ParticipationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipationResponseDTO(
        UUID id,
        UUID eventId,
        String username,
        ParticipationStatus status,
        LocalDateTime joinedAt
) {
}
