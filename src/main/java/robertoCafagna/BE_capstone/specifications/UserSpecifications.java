package robertoCafagna.BE_capstone.specifications;

import org.springframework.data.jpa.domain.Specification;
import robertoCafagna.BE_capstone.entities.User;

public class UserSpecifications {

    public static Specification<User> usernameOrEmailContains(String query) {
        return (root, cq, cb) -> {
            String like = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("email")), like)
            );
        };
    }

    public static Specification<User> isActiveOnly() {
        return (root, cq, cb) -> cb.and(
                cb.isTrue(root.get("active")),
                cb.isNull(root.get("deletedAt"))
        );
    }

    public static Specification<User> isDeactivatedOnly() {
        return (root, cq, cb) -> cb.and(
                cb.isFalse(root.get("active")),
                cb.isNull(root.get("deletedAt"))
        );
    }

    public static Specification<User> isDeleted() {
        return (root, cq, cb) -> cb.isNotNull(root.get("deletedAt"));
    }
}
