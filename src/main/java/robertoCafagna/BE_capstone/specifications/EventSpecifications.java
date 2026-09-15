package robertoCafagna.BE_capstone.specifications;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import robertoCafagna.BE_capstone.entities.Event;
import robertoCafagna.BE_capstone.entities.Participation;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventVisibility;
import robertoCafagna.BE_capstone.enums.ParticipationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EventSpecifications {

    public static Specification<Event> visibilityIn(List<EventVisibility> visibilities) {
        return (root, query, cb) -> root.get("visibility").in(visibilities);
    }

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Event> titleContains(String title) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Event> startDateAfter(LocalDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDateTime"), from);
    }

    public static Specification<Event> startDateBefore(LocalDateTime to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDateTime"), to);
    }

    public static Specification<Event> withinBoundingBox(double minLat, double maxLat, double minLng, double maxLng) {
        return (root, query, cb) -> cb.and(
                cb.between(root.get("meetingPointLat"), minLat, maxLat),
                cb.between(root.get("meetingPointLng"), minLng, maxLng)
        );
    }

    public static Specification<Event> hasNoParent() {
        return (root, query, cb) -> cb.isNull(root.get("parentEvent"));
    }

    public static Specification<Event> notEnded(LocalDateTime now) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endDateTime"), now);
    }

    public static Specification<Event> hasOrganizer(UUID organizerId) {
        return (root, query, cb) -> cb.equal(root.get("organizer").get("id"), organizerId);
    }

    public static Specification<Event> isCurrent(LocalDateTime now) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), EventStatus.ACTIVE),
                cb.greaterThan(root.get("endDateTime"), now)
        );
    }

    public static Specification<Event> isHistory(LocalDateTime now) {
        return (root, query, cb) -> cb.or(
                cb.notEqual(root.get("status"), EventStatus.ACTIVE),
                cb.lessThanOrEqualTo(root.get("endDateTime"), now)
        );
    }

    public static Specification<Event> organizedOrParticipatedBy(UUID userId) {
        return (root, query, cb) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<Participation> pRoot = subquery.from(Participation.class);
            subquery.select(pRoot.get("event").get("id"))
                    .where(cb.and(
                            cb.equal(pRoot.get("user").get("id"), userId),
                            pRoot.get("status").in(List.of(ParticipationStatus.PENDING, ParticipationStatus.ACCEPTED))
                    ));

            return cb.or(
                    cb.equal(root.get("organizer").get("id"), userId),
                    root.get("id").in(subquery)
            );
        };
    }
}
