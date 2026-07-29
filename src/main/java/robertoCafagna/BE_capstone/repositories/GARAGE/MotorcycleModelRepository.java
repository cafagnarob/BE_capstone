package robertoCafagna.BE_capstone.repositories.GARAGE;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.MotorcycleModel;

import java.util.UUID;

@Repository
public interface MotorcycleModelRepository
        extends JpaRepository<MotorcycleModel, UUID> {

    Page<MotorcycleModel> findByBrandId(UUID brandId, Pageable pageable);

    Page<MotorcycleModel> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByBrandIdAndName(
            UUID brandId,
            String name
    );

    boolean existsByBrandName(String brandName);
}
