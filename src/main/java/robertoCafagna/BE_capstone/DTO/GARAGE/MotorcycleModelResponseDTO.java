package robertoCafagna.BE_capstone.DTO.GARAGE;

import robertoCafagna.BE_capstone.enums.MotorcycleCategory;

import java.util.UUID;

public record MotorcycleModelResponseDTO(
        UUID id,
        BrandResponseDTO brand,
        String name,
        int engineCc,
        MotorcycleCategory category,
        int yearStart,
        Integer yearEnd,
        Integer horsePower,
        Integer weightKg,
        String imageUrl
) {
}
