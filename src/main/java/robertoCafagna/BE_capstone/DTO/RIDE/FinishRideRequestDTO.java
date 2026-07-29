package robertoCafagna.BE_capstone.DTO.RIDE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record FinishRideRequestDTO(
        @NotNull(message = "Specificare l'orario di fine")
        LocalDateTime endedAt,

        @NotNull(message = "Specificare la distanza percorsa")
        Double distanceKm,

        Double avgSpeedKmH,

        Double maxSpeedKmH,

        int stopsCount,

        int totalStopDurationSeconds,

        String notes,

        @NotEmpty(message = "Il percorso deve contenere almeno un punto GPS")
        @Valid
        List<RidePointRequestDTO> points
) {
}
