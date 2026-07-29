package robertoCafagna.BE_capstone.DTO.EVENT;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import robertoCafagna.BE_capstone.enums.EventVisibility;

import java.time.LocalDateTime;

public record CreateEventRequestDTO(
        @NotBlank(message = "Inserire un titolo")
        String title,
        @NotBlank(message = "Inserire una descrizione")
        String description,
        @NotNull(message = "Specificare data/ora di inizio")
        @Future(message = "La data di inizio deve essere futura")
        LocalDateTime startDateTime,
        @NotNull(message = "Specificare data/ora di fine")
        LocalDateTime endDateTime,
        @NotNull(message = "Specificare la latitudine del ritrovo")
        Double meetingPointLat,
        @NotNull(message = "Specificare la longitudine del ritrovo")
        Double meetingPointLng,
        @Min(value = 1, message = "Il numero massimo di partecipanti deve essere almeno 1")
        int maxParticipants,
        @NotNull(message = "Specificare la visibilità")
        EventVisibility visibility,
        boolean autoApprove,
        String accessCode
) {
}
