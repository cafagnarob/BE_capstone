package robertoCafagna.BE_capstone.DTO.RIDE;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RidePointRequestDTO(
        @NotNull(message = "Latitudine obbligatoria")
        Double latitude,

        @NotNull(message = "Longitudine obbligatoria")
        Double longitude,

        int sequence,

        Double speedKmh,

        Double altitude,

        @NotNull(message = "Timestamp obbligatorio")
        LocalDateTime recordedAt
) {
}
