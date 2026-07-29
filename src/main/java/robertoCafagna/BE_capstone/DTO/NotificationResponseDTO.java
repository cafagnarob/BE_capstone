package robertoCafagna.BE_capstone.DTO;

import robertoCafagna.BE_capstone.enums.NotificationType;
import robertoCafagna.BE_capstone.enums.ReferenceType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDTO(
        UUID id,
        NotificationType type,
        String message,
        boolean read,
        UUID referenceId,
        ReferenceType referenceType,
        LocalDateTime createdAt
) {
}
