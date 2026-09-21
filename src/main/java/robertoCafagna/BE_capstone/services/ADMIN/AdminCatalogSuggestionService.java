package robertoCafagna.BE_capstone.services.ADMIN;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminCatalogSuggestionDTO;
import robertoCafagna.BE_capstone.entities.CatalogSuggestion;
import robertoCafagna.BE_capstone.enums.CatalogSuggestionStatus;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.GARAGE.CatalogSuggestionRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCatalogSuggestionService {
    private final CatalogSuggestionRepository catalogSuggestionRepository;

    public Page<AdminCatalogSuggestionDTO> getAll(CatalogSuggestionStatus status, int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<CatalogSuggestion> results = status != null
                ? catalogSuggestionRepository.findByStatus(status, pageable)
                : catalogSuggestionRepository.findAll(pageable);

        return results.map(this::toDTO);
    }

    public void updateStatus(UUID id, CatalogSuggestionStatus newStatus) {
        CatalogSuggestion suggestion = catalogSuggestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));
        suggestion.setStatus(newStatus);
        catalogSuggestionRepository.save(suggestion);
        log.info("Segnalazione catalogo {} impostata a {}", id, newStatus);
    }

    private AdminCatalogSuggestionDTO toDTO(CatalogSuggestion s) {
        return new AdminCatalogSuggestionDTO(
                s.getId(), s.getReporter().getUsername(), s.getBrandName(), s.getModelName(),
                s.getEngineCc(), s.getCategory(), s.getYearStart(), s.getYearEnd(),
                s.getHorsePower(), s.getWeightKg(), s.getNote(), s.getStatus(), s.getCreatedAt()
        );
    }
}
