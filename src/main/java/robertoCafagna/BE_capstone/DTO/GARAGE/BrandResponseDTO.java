package robertoCafagna.BE_capstone.DTO.GARAGE;

import java.util.UUID;

public record BrandResponseDTO(
        UUID id,
        String name,
        String logoUrl
) {
}
