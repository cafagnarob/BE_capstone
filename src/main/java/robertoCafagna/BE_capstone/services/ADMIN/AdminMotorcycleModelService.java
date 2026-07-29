package robertoCafagna.BE_capstone.services.ADMIN;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.GARAGE.CreateMotorcycleModelRequestDTO;
import robertoCafagna.BE_capstone.DTO.GARAGE.BrandResponseDTO;
import robertoCafagna.BE_capstone.DTO.GARAGE.MotorcycleModelResponseDTO;
import robertoCafagna.BE_capstone.entities.Brand;
import robertoCafagna.BE_capstone.entities.MotorcycleModel;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.GARAGE.BrandRepository;
import robertoCafagna.BE_capstone.repositories.GARAGE.MotorcycleModelRepository;
import robertoCafagna.BE_capstone.services.CloudinaryService;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AdminMotorcycleModelService {

    private final MotorcycleModelRepository motorcycleModelRepository;
    private final BrandRepository brandRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public MotorcycleModelResponseDTO createModel(CreateMotorcycleModelRequestDTO body, MultipartFile image) {
        Brand brand = brandRepository.findById(body.brandId())
                .orElseThrow(() -> new NotFoundException("Brand non trovato"));

        if (motorcycleModelRepository.existsByBrandIdAndName(body.brandId(), body.name())) {
            throw new BadRequestException("Questo brand ha già un modello con questo nome");
        }

        String imageUrl = null;
        String imagePublicId = null;
        if (image != null && !image.isEmpty()) {
            try {
                CloudinaryService.UploadResult result =
                        cloudinaryService.uploadImage(image, "riders-app/models");
                imageUrl = result.url();
                imagePublicId = result.publicId();
            } catch (IOException e) {
                throw new BadRequestException("Errore durante il caricamento dell'immagine");
            }
        }

        MotorcycleModel model = new MotorcycleModel(
                brand, body.name(), body.engineCc(), body.category(),
                body.yearStart(), body.yearEnd(), body.horsePower(), body.weightKg(),
                imageUrl
        );
        model.setImagePublicId(imagePublicId);

        motorcycleModelRepository.save(model);
        return toDTO(model);
    }

    private MotorcycleModelResponseDTO toDTO(MotorcycleModel model) {
        return new MotorcycleModelResponseDTO(
                model.getId(),
                new BrandResponseDTO(
                        model.getBrand().getId(), model.getBrand().getName(), model.getBrand().getLogoUrl()
                ),
                model.getName(), model.getEngineCc(), model.getCategory(),
                model.getYearStart(), model.getYearEnd(), model.getHorsePower(), model.getWeightKg(),
                model.getImageUrl()
        );
    }
}
