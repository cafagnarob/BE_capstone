package robertoCafagna.BE_capstone.DTO.EVENT;

import jakarta.validation.constraints.NotBlank;

public record RegenerateAccessCodeRequestDTO(
        @NotBlank(message = "Inserire il nuovo codice di accesso")
        String newAccessCode
) {
}
