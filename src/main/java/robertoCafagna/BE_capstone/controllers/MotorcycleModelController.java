package robertoCafagna.BE_capstone.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import robertoCafagna.BE_capstone.DTO.GARAGE.MotorcycleModelResponseDTO;
import robertoCafagna.BE_capstone.enums.MotorcycleCategory;
import robertoCafagna.BE_capstone.services.GARAGE.MotorcycleModelService;

import java.util.UUID;

@RestController
@RequestMapping("/motorcycle-models")
@RequiredArgsConstructor
public class MotorcycleModelController {

    private final MotorcycleModelService motorcycleModelService;

    @GetMapping
    public ResponseEntity<Page<MotorcycleModelResponseDTO>> getAll(
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) MotorcycleCategory category,
            @RequestParam(required = false) Integer minCc,
            @RequestParam(required = false) Integer maxCc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String orderBy
    ) {
        return ResponseEntity.ok(
                motorcycleModelService.findFiltered(brandId, name, category, minCc, maxCc, page, size, orderBy)
        );
    }
}
