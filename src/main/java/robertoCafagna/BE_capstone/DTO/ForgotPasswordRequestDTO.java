package robertoCafagna.BE_capstone.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDTO(
        @NotBlank(message = "Inserire l'email")
        @Email(message = "Email non valida")
        String email
) {
}
