package robertoCafagna.BE_capstone.DTO.EVENT;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import robertoCafagna.BE_capstone.enums.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddEventDayRequestDTO(
        @NotBlank(message = "Inserire un titolo")
        String title,
        @NotBlank(message = "Inserire una descrizione")
        String description,
        @NotNull(message = "Specificare il tipo di giorno")
        EventType type,
        UUID routeId,
        Double meetingPointLat,
        Double meetingPointLng,
        @NotNull(message = "Specificare data/ora di inizio")
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer bufferMinutes
) {
}
