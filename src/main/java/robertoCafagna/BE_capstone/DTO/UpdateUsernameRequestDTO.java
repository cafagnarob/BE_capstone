package robertoCafagna.BE_capstone.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUsernameRequestDTO(
        @NotBlank(message = "Inserire il nuovo username")
        @Size(min = 3, max = 30, message = "Lo username deve avere tra 3 e 30 caratteri")
        String newUsername,

        @NotBlank(message = "Inserire la password attuale per confermare")
        String currentPassword
) {
}
