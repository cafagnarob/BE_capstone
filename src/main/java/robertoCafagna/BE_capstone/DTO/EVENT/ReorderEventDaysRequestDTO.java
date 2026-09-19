package robertoCafagna.BE_capstone.DTO.EVENT;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ReorderEventDaysRequestDTO(
        @NotEmpty(message = "Specificare l'elenco dei giorni nel nuovo ordine")
        List<UUID> dayIds
) {
}
