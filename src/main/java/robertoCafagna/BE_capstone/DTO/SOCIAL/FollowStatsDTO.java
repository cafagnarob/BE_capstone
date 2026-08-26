package robertoCafagna.BE_capstone.DTO.SOCIAL;

public record FollowStatsDTO(
        long followersCount,
        long followingCount,
        boolean isFollowedByCurrentUser
) {
}
