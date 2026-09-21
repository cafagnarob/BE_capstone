package robertoCafagna.BE_capstone.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminCatalogSuggestionDTO;
import robertoCafagna.BE_capstone.DTO.ADMIN.UpdateCatalogSuggestionStatusRequestDTO;
import robertoCafagna.BE_capstone.enums.CatalogSuggestionStatus;
import robertoCafagna.BE_capstone.services.ADMIN.AdminCatalogSuggestionService;

import java.util.UUID;

@RestController
@RequestMapping("/admin/catalog-suggestions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogSuggestionController {
    private final AdminCatalogSuggestionService adminCatalogSuggestionService;

    @GetMapping
    public ResponseEntity<Page<AdminCatalogSuggestionDTO>> getAll(
            @RequestParam(required = false) CatalogSuggestionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminCatalogSuggestionService.getAll(status, page, size));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCatalogSuggestionStatusRequestDTO body
    ) {
        adminCatalogSuggestionService.updateStatus(id, body.status());
        return ResponseEntity.noContent().build();
    }
}
