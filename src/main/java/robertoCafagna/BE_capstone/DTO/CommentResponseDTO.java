package robertoCafagna.BE_capstone.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponseDTO(
        UUID id,
        String authorUsername,
        String authorProfilePicture,
        String text,
        LocalDateTime createdAt
) {
}
