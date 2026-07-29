package robertoCafagna.BE_capstone.repositories.GARAGE;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Brand;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository
        extends JpaRepository<Brand, UUID> {
    Optional<Brand> findByName(String name);

    boolean existsByName(String name);
}
