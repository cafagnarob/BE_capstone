package robertoCafagna.BE_capstone.DTO.ADMIN;

import jakarta.validation.constraints.NotNull;
import robertoCafagna.BE_capstone.enums.CatalogSuggestionStatus;

public record UpdateCatalogSuggestionStatusRequestDTO(
        @NotNull(message = "Specificare il nuovo stato")
        CatalogSuggestionStatus status
) {
}