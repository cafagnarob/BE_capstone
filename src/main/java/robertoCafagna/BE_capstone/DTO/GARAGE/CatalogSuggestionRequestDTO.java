package robertoCafagna.BE_capstone.DTO.GARAGE;

import jakarta.validation.constraints.NotBlank;
import robertoCafagna.BE_capstone.enums.MotorcycleCategory;

public record CatalogSuggestionRequestDTO(
        @NotBlank(message = "Inserire il nome del brand")
        String brandName,
        String modelName,
        Integer engineCc,
        MotorcycleCategory category,
        Integer yearStart,
        Integer yearEnd,
        Integer horsePower,
        Integer weightKg,
        String note
) {
}
