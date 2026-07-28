package robertoCafagna.BE_capstone.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateVehicleRequestDTO(
        @NotNull(message = "Specificare il modello")
        UUID modelId,

        String nickname,

        @Min(value = 1900, message = "Anno non valido")
        int year,

        String licensePlate,
        String vin,
        String color,

        @Min(value = 0, message = "Il chilometraggio iniziale non può essere negativo")
        int initialMileage
) {
}
