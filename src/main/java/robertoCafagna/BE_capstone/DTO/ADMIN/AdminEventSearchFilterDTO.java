package robertoCafagna.BE_capstone.DTO.ADMIN;

import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventType;
import robertoCafagna.BE_capstone.enums.EventVisibility;

import java.time.LocalDateTime;

public record AdminEventSearchFilterDTO(
        String title,
        EventStatus status,
        EventVisibility visibility,
        EventType type,
        String organizerUsername,
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        Double lat,
        Double lng,
        Double radiusKm,
        boolean onlyPendingAccessRequests
) {
}
