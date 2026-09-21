package robertoCafagna.BE_capstone.DTO.ADMIN;

import java.util.List;

public record AdminDashboardDTO(
        long totalUsers, long activeUsers, long newUsersLast30Days,
        long activeLast7Days, long neverLoggedIn, long unverifiedEmails,
        long deactivatedUsers, long deletedUsers,
        long totalPosts, long totalRides, double totalKmRidden, long totalRoutes,
        long totalEvents, long standardEvents, long radunoEvents, long multiDayTrips,
        long pendingContentReports, long pendingCatalogSuggestions,
        List<TrendPointDTO> newUsersPerWeek, List<TrendPointDTO> kmRiddenPerWeek
) {
}
