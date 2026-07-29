package robertoCafagna.BE_capstone.DTO.SOCIAL;

import java.util.UUID;

public record FollowUserSummaryDTO(
        UUID id,
        String username,
        String name,
        String surname,
        String profilePicture
) {
}
