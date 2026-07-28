package robertoCafagna.BE_capstone.DTO;

import java.util.UUID;

public record VehicleSummaryDTO(
        UUID id,
        String nickname,
        String photoUrl
) {
}
