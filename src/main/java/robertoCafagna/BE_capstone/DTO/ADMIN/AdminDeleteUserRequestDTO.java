package robertoCafagna.BE_capstone.DTO.ADMIN;

import jakarta.validation.constraints.NotBlank;

public record AdminDeleteUserRequestDTO(
        @NotBlank(message = "Specificare un motivo")
        String reason
) {
}
