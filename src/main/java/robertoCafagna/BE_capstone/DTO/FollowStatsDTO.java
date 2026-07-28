package robertoCafagna.BE_capstone.DTO;

public record FollowStatsDTO(
        long followersCount,
        long followingCount,
        boolean isFollowedByCurrentUser
) {
}
