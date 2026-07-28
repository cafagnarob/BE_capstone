package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.BrandResponseDTO;
import robertoCafagna.BE_capstone.entities.Brand;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.repositories.BrandRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AdminBrandService {

    private final BrandRepository brandRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public BrandResponseDTO createBrand(String name, MultipartFile logo) {
        if (brandRepository.existsByName(name)) {
            throw new BadRequestException("Un brand con questo nome esiste già");
        }

        CloudinaryService.UploadResult result;
        try {
            result = cloudinaryService.uploadImage(logo, "riders-app/brands/logos");
        } catch (IOException e) {
            throw new BadRequestException("Errore durante il caricamento del logo");
        }

        Brand brand = new Brand(name, result.url());
        brand.setLogoPublicId(result.publicId());
        brandRepository.save(brand);

        return toDTO(brand);
    }

    private BrandResponseDTO toDTO(Brand brand) {
        return new BrandResponseDTO(brand.getId(), brand.getName(), brand.getLogoUrl());
    }
}
