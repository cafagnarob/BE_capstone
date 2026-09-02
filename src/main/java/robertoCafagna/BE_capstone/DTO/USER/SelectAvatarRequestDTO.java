package robertoCafagna.BE_capstone.DTO.USER;

import jakarta.validation.constraints.NotBlank;

public record SelectAvatarRequestDTO(
        @NotBlank(message = "Specificare un avatar")
        String avatarUrl
) {
}