package robertoCafagna.BE_capstone.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
        @NotBlank(message = "Inserire la password attuale")
        String oldPassword,

        @NotBlank(message = "Inserire la nuova password")
        @Size(min = 8, message = "La nuova password deve avere almeno 8 caratteri")
        String newPassword
) {
}
