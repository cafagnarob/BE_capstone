package robertoCafagna.BE_capstone.DTO.EVENT;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import robertoCafagna.BE_capstone.enums.EventType;
import robertoCafagna.BE_capstone.enums.EventVisibility;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateEventRequestDTO(
        @NotBlank(message = "Inserire un titolo")
        String title,
        @NotBlank(message = "Inserire una descrizione")
        String description,
        @NotNull(message = "Specificare il tipo di evento")
        EventType type,
        UUID routeId,
        Double meetingPointLat,
        Double meetingPointLng,
        @NotNull(message = "Specificare data/ora di inizio")
        @Future(message = "La data di inizio deve essere futura")
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer bufferMinutes,
        @Min(value = 1, message = "Il numero massimo di partecipanti deve essere almeno 1")
        int maxParticipants,
        @NotNull(message = "Specificare la visibilità")
        EventVisibility visibility,
        boolean autoApprove,
        String accessCode
) {
}
