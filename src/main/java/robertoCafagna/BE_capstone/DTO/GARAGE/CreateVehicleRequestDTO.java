package robertoCafagna.BE_capstone.DTO.GARAGE;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CreateVehicleRequestDTO(
        @NotNull(message = "Specificare il modello")
        UUID modelId,

        String nickname,

        @Min(value = 1900, message = "Anno non valido")
        int year,


        @Pattern(regexp = "^[A-Za-z]{2}[0-9]{5}$", message = "Formato targa non valido")
        String licensePlate,
        String vin,
        String color,

        @Min(value = 0, message = "Il chilometraggio iniziale non può essere negativo")
        int initialMileage
) {
}
