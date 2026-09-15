package robertoCafagna.BE_capstone.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import robertoCafagna.BE_capstone.DTO.GARAGE.CatalogSuggestionRequestDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.GARAGE.CatalogSuggestionService;

@RestController
@RequestMapping("/catalog-suggestions")
@RequiredArgsConstructor
public class CatalogSuggestionController {

    private final CatalogSuggestionService catalogSuggestionService;

    @PostMapping
    public ResponseEntity<Void> submit(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid CatalogSuggestionRequestDTO body
    ) {
        catalogSuggestionService.submit(currentUser, body);
        return ResponseEntity.accepted().build();
    }
}
