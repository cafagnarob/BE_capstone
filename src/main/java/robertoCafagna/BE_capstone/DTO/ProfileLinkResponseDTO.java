package robertoCafagna.BE_capstone.DTO;

import robertoCafagna.BE_capstone.enums.Platform;

import java.util.UUID;

public record ProfileLinkResponseDTO(
        UUID id,
        Platform platform,
        String url
) {
}
