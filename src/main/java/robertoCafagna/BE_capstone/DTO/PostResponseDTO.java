package robertoCafagna.BE_capstone.DTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostResponseDTO(
        UUID id,
        String authorUsername,
        String authorProfilePicture,
        String text,
        LocalDateTime createdAt,
        EventSummaryDTO event,
        RideSummaryDTO ride,
        List<PostMediaResponseDTO> media,
        long likeCount,
        long commentCount,
        boolean likedByCurrentUser
) {
}
