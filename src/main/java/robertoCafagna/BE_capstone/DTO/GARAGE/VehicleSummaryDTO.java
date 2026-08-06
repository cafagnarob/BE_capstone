package robertoCafagna.BE_capstone.DTO.GARAGE;

import java.util.UUID;

public record VehicleSummaryDTO(
        UUID id,
        String nickname,
        String photoUrl,
        String brandName,
        String modelName
) {
}
