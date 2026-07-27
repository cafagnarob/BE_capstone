package robertoCafagna.BE_capstone.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.MotorcycleModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface MotorcycleModelRepository
        extends JpaRepository<MotorcycleModel, UUID> {

    List<MotorcycleModel> findByBrandId(UUID brandId);

    List<MotorcycleModel> findByNameContainingIgnoreCase(String name);
}
