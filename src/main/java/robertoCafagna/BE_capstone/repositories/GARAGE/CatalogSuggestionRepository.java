package robertoCafagna.BE_capstone.repositories.GARAGE;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.CatalogSuggestion;
import robertoCafagna.BE_capstone.enums.CatalogSuggestionStatus;

import java.util.UUID;

@Repository
public interface CatalogSuggestionRepository extends JpaRepository<CatalogSuggestion, UUID> {
    Page<CatalogSuggestion> findByStatus(CatalogSuggestionStatus status, Pageable pageable);

    long countByStatus(CatalogSuggestionStatus status);
}
