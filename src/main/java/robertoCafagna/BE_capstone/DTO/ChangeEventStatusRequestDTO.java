package robertoCafagna.BE_capstone.DTO;

import jakarta.validation.constraints.NotNull;
import robertoCafagna.BE_capstone.enums.EventStatus;

public record ChangeEventStatusRequestDTO(
        @NotNull(message = "Specificare il nuovo stato")
        EventStatus status
) {
}
