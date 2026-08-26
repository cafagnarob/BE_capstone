package robertoCafagna.BE_capstone.DTO.SOCIAL;

import robertoCafagna.BE_capstone.enums.MediaType;

import java.util.UUID;

public record PostMediaResponseDTO(
        UUID id,
        String mediaUrl,
        MediaType type,
        int orderIndex
) {
}
