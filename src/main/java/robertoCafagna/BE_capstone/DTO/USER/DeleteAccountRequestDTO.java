package robertoCafagna.BE_capstone.DTO.USER;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequestDTO(
        @NotBlank(message = "Inserisci la password per confermare")
        String currentPassword
) {
}
