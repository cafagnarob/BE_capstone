package robertoCafagna.BE_capstone.DTO.USER;

import java.util.UUID;

public record UserSearchResultDTO(
        UUID id,
        String username,
        String name,
        String surname,
        String profilePicture
) {
}
