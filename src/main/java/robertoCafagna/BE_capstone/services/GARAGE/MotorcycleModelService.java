package robertoCafagna.BE_capstone.services.GARAGE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.GARAGE.BrandResponseDTO;
import robertoCafagna.BE_capstone.DTO.GARAGE.MotorcycleModelResponseDTO;
import robertoCafagna.BE_capstone.entities.Brand;
import robertoCafagna.BE_capstone.entities.MotorcycleModel;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.repositories.GARAGE.MotorcycleModelRepository;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotorcycleModelService {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("name", "engineCc", "yearStart", "horsePower", "weightKg");

    private final MotorcycleModelRepository motorcycleModelRepository;

    private Pageable buildPageable(int page, int size, String orderBy) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        if (!ALLOWED_SORT_FIELDS.contains(orderBy)) {
            throw new BadRequestException(
                    "Campo di ordinamento non valido. Valori ammessi: " + ALLOWED_SORT_FIELDS
            );
        }
        return PageRequest.of(page, size, Sort.by(orderBy));
    }

    public Page<MotorcycleModelResponseDTO> getAll(int page, int size, String orderBy) {
        Pageable pageable = buildPageable(page, size, orderBy);
        return motorcycleModelRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<MotorcycleModelResponseDTO> getByBrand(UUID brandId, int page, int size, String orderBy) {
        Pageable pageable = buildPageable(page, size, orderBy);
        return motorcycleModelRepository.findByBrandId(brandId, pageable).map(this::toDTO);
    }

    public Page<MotorcycleModelResponseDTO> search(String name, int page, int size, String orderBy) {
        Pageable pageable = buildPageable(page, size, orderBy);
        return motorcycleModelRepository.findByNameContainingIgnoreCase(name, pageable).map(this::toDTO);
    }

    private MotorcycleModelResponseDTO toDTO(MotorcycleModel model) {
        return new MotorcycleModelResponseDTO(
                model.getId(),
                toBrandDTO(model.getBrand()),
                model.getName(),
                model.getEngineCc(),
                model.getCategory(),
                model.getYearStart(),
                model.getYearEnd(),
                model.getHorsePower(),
                model.getWeightKg(),
                model.getImageUrl()
        );
    }

    private BrandResponseDTO toBrandDTO(Brand brand) {
        return new BrandResponseDTO(brand.getId(), brand.getName(), brand.getLogoUrl());
    }

}
