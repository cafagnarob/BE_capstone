package robertoCafagna.BE_capstone.specifications;

import org.springframework.data.jpa.domain.Specification;
import robertoCafagna.BE_capstone.entities.MotorcycleModel;
import robertoCafagna.BE_capstone.enums.MotorcycleCategory;

import java.util.UUID;

public class MotorcycleModelSpecifications {
    public static Specification<MotorcycleModel> hasBrand(UUID brandId) {
        return (root, query, cb) -> cb.equal(root.get("brand").get("id"), brandId);
    }

    public static Specification<MotorcycleModel> nameContains(String name) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<MotorcycleModel> hasCategory(MotorcycleCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<MotorcycleModel> engineCcGreaterThanOrEqual(int minCc) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("engineCc"), minCc);
    }

    public static Specification<MotorcycleModel> engineCcLessThanOrEqual(int maxCc) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("engineCc"), maxCc);
    }
}
