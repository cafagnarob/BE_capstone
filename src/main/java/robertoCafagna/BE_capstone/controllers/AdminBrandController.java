package robertoCafagna.BE_capstone.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.GARAGE.BrandResponseDTO;
import robertoCafagna.BE_capstone.services.ADMIN.AdminBrandService;

@RestController
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBrandController {

    private final AdminBrandService adminBrandService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BrandResponseDTO> create(
            @RequestParam("name") String name,
            @RequestParam("logo") MultipartFile logo
    ) {
        BrandResponseDTO created = adminBrandService.createBrand(name, logo);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
