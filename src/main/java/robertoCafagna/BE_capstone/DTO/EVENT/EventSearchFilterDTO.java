package robertoCafagna.BE_capstone.DTO.EVENT;

import java.time.LocalDateTime;

public record EventSearchFilterDTO(
        String title,
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        Double lat,
        Double lng,
        Double radiusKm
) {
}
