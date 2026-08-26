package robertoCafagna.BE_capstone.DTO.USER;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import robertoCafagna.BE_capstone.enums.Platform;

public record ProfileLinkRequestDTO(
        @NotNull(message = "Specificare la piattaforma")
        Platform platform,

        @NotBlank(message = "Inserire l'URL") String url
) {
}
