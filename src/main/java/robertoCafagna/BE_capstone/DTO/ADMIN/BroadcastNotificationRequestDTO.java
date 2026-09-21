package robertoCafagna.BE_capstone.DTO.ADMIN;

import jakarta.validation.constraints.NotBlank;

public record BroadcastNotificationRequestDTO(
        @NotBlank(message = "Il messaggio non può essere vuoto")
        String message
) {
}