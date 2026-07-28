package robertoCafagna.BE_capstone.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmailRequestDTO(
        @NotBlank(message = "Inserire la nuova email")
        @Email(message = "Email non valida")
        String newEmail,

        @NotBlank(message = "Inserire la password attuale per confermare")
        String currentPassword
) {
}
