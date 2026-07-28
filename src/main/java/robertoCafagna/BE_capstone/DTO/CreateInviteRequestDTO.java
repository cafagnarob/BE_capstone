package robertoCafagna.BE_capstone.DTO;

import jakarta.validation.constraints.NotBlank;

public record CreateInviteRequestDTO(
        @NotBlank(message = "Specificare lo username da invitare")
        String username
) {
}
