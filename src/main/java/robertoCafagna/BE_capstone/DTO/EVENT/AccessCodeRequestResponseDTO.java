package robertoCafagna.BE_capstone.DTO.EVENT;

import robertoCafagna.BE_capstone.enums.AccessRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccessCodeRequestResponseDTO(
        UUID id,
        String requesterUsername,
        String requesterProfilePicture,
        AccessRequestStatus status,
        LocalDateTime createdAt
) {
}