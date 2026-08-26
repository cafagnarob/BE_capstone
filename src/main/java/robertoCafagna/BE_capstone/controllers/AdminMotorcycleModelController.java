package robertoCafagna.BE_capstone.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.GARAGE.CreateMotorcycleModelRequestDTO;
import robertoCafagna.BE_capstone.DTO.GARAGE.MotorcycleModelResponseDTO;
import robertoCafagna.BE_capstone.services.ADMIN.AdminMotorcycleModelService;

@RestController
@RequestMapping("/admin/motorcycle-models")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMotorcycleModelController {

    private final AdminMotorcycleModelService adminMotorcycleModelService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MotorcycleModelResponseDTO> create(
            @RequestPart("data") @Valid CreateMotorcycleModelRequestDTO body,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        MotorcycleModelResponseDTO created = adminMotorcycleModelService.createModel(body, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
