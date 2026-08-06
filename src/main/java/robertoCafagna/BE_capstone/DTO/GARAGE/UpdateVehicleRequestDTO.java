package robertoCafagna.BE_capstone.DTO.GARAGE;

import jakarta.validation.constraints.Pattern;

public record UpdateVehicleRequestDTO(
        String nickname,

        Integer year,

        @Pattern(regexp = "^[A-Za-z]{2}[0-9]{5}$", message = "Formato targa non valido")
        String licensePlate,

        String vin,

        String color
) {
}
