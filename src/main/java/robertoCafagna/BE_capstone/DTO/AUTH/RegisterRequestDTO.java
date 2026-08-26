package robertoCafagna.BE_capstone.DTO.AUTH;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "inserire uno username")
        String username,

        @NotBlank(message = "inserire il nome")
        String name,

        @NotBlank(message = "inserire il cognome")
        String surname,

        @NotBlank(message = "inserire l' email")
        @Email
        String email,

        @NotBlank(message = "inserire la password")
        @Size(min = 8, message = "La password deve avere almeno 8 caratteri")
        String password) {
}
