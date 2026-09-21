package robertoCafagna.BE_capstone.DTO.ADMIN;

import robertoCafagna.BE_capstone.enums.CatalogSuggestionStatus;
import robertoCafagna.BE_capstone.enums.MotorcycleCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminCatalogSuggestionDTO(
        UUID id, String reporterUsername, String brandName, String modelName,
        Integer engineCc, MotorcycleCategory category, Integer yearStart, Integer yearEnd,
        Integer horsePower, Integer weightKg, String note,
        CatalogSuggestionStatus status, LocalDateTime createdAt
) {
}
