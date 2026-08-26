package robertoCafagna.BE_capstone.specifications;

import org.springframework.data.jpa.domain.Specification;
import robertoCafagna.BE_capstone.entities.Event;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventVisibility;

import java.time.LocalDateTime;
import java.util.List;

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
}
