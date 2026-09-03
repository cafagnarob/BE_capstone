package robertoCafagna.BE_capstone.DTO.SOCIAL;

import robertoCafagna.BE_capstone.enums.WidgetSize;
import robertoCafagna.BE_capstone.enums.WidgetType;

import java.util.UUID;

public record PostWidgetResponseDTO(
        UUID id,
        UUID mediaId,
        WidgetType type,
        UUID referenceId,
        WidgetSize size,
        double xPercent,
        double yPercent
) {
}
