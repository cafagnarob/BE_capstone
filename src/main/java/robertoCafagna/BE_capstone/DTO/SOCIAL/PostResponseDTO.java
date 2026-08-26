package robertoCafagna.BE_capstone.DTO.SOCIAL;

import robertoCafagna.BE_capstone.DTO.EVENT.EventSummaryDTO;
import robertoCafagna.BE_capstone.DTO.GARAGE.VehicleSummaryDTO;
import robertoCafagna.BE_capstone.DTO.RIDE.RideSummaryDTO;

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
        VehicleSummaryDTO vehicle,
        List<PostMediaResponseDTO> media,
        long likeCount,
        long commentCount,
        boolean likedByCurrentUser,
        UUID routeId,
        String routeName,
        Double routeDistanceMeters
) {
}
