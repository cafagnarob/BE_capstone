package robertoCafagna.BE_capstone.services.ADMIN;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminDashboardDTO;
import robertoCafagna.BE_capstone.DTO.ADMIN.TrendPointDTO;
import robertoCafagna.BE_capstone.Interface.RideTrendPoint;
import robertoCafagna.BE_capstone.enums.CatalogSuggestionStatus;
import robertoCafagna.BE_capstone.enums.EventType;
import robertoCafagna.BE_capstone.enums.ReportStatus;
import robertoCafagna.BE_capstone.repositories.EVENT.EventRepository;
import robertoCafagna.BE_capstone.repositories.GARAGE.CatalogSuggestionRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RideRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RouteRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.ReportRepository;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private static final int TREND_WEEKS = 12;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RideRepository rideRepository;
    private final PostRepository postRepository;
    private final RouteRepository routeRepository;
    private final ReportRepository reportRepository;
    private final CatalogSuggestionRepository catalogSuggestionRepository;

    public AdminDashboardDTO getDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last30Days = now.minusDays(30);
        LocalDateTime trendSince = now.minusWeeks(TREND_WEEKS);

        List<LocalDateTime> newUserDates = userRepository.findCreatedAtSince(trendSince);
        List<RideTrendPoint> rideTrendData = rideRepository.findTrendDataSince(trendSince);

        LocalDateTime last7Days = now.minusDays(7);

        return new AdminDashboardDTO(
                userRepository.count(),
                userRepository.countByActiveTrue(),
                userRepository.countByCreatedAtAfter(last30Days),
                userRepository.countByLastLoginAfter(last7Days),
                userRepository.countByLastLoginIsNull(),
                userRepository.countByEmailVerifiedFalse(),
                userRepository.countByActiveFalseAndDeletedAtIsNull(),
                userRepository.countByDeletedAtIsNotNull(),
                postRepository.count(),
                rideRepository.count(),
                rideRepository.sumTotalDistanceKm(),
                routeRepository.count(),
                eventRepository.countByParentEventIsNull(),
                eventRepository.countByTypeAndParentEventIsNull(EventType.STANDARD),
                eventRepository.countByTypeAndParentEventIsNull(EventType.RADUNO),
                eventRepository.countByTypeAndParentEventIsNull(EventType.MULTI_DAY_TRIP),
                reportRepository.countByStatus(ReportStatus.PENDING),
                catalogSuggestionRepository.countByStatus(CatalogSuggestionStatus.PENDING),
                bucketDatesByWeek(newUserDates),
                bucketRideDistanceByWeek(rideTrendData)
        );
    }

    private List<TrendPointDTO> bucketDatesByWeek(List<LocalDateTime> dates) {
        LocalDate currentWeekStart = mondayOf(LocalDate.now());
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (int i = TREND_WEEKS - 1; i >= 0; i--) {
            counts.put(currentWeekStart.minusWeeks(i), 0L);
        }
        for (LocalDateTime date : dates) {
            LocalDate weekStart = mondayOf(date.toLocalDate());
            counts.merge(weekStart, 1L, Long::sum);
        }
        return counts.entrySet().stream()
                .map(e -> new TrendPointDTO(e.getKey(), e.getValue()))
                .toList();
    }

    private List<TrendPointDTO> bucketRideDistanceByWeek(List<RideTrendPoint> points) {
        LocalDate currentWeekStart = mondayOf(LocalDate.now());
        Map<LocalDate, Double> sums = new LinkedHashMap<>();
        for (int i = TREND_WEEKS - 1; i >= 0; i--) {
            sums.put(currentWeekStart.minusWeeks(i), 0.0);
        }
        for (RideTrendPoint p : points) {
            if (p.getCreatedAt() == null) continue;
            LocalDate weekStart = mondayOf(p.getCreatedAt().toLocalDate());
            double km = p.getDistanceKm() != null ? p.getDistanceKm() : 0.0;
            sums.merge(weekStart, km, Double::sum);
        }
        return sums.entrySet().stream()
                .map(e -> new TrendPointDTO(e.getKey(), e.getValue()))
                .toList();
    }

    private LocalDate mondayOf(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }
}
