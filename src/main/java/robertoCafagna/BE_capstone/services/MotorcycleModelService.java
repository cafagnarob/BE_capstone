package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.entities.MotorcycleModel;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.MotorcycleModelRepository;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotorcycleModelService {
    private final MotorcycleModelRepository motorcycleModelRepository;
    private final BrandService brandService;

    public MotorcycleModel findById(UUID id) {
        return motorcycleModelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Modello moto non trovato")
                );
    }


    public Page<MotorcycleModel> getAll(int page, int size, String orderBy) {
        if (size <= 0 || size > 20) {
            size = 10;
        }
        if (page < 0) {
            page = 0;
        }
        if (orderBy == null || orderBy.isBlank()) {
            orderBy = "name";
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(orderBy)
        );
        return motorcycleModelRepository.findAll(pageable);
    }

    public MotorcycleModel save(
            MotorcycleModel model
    ) {
        if (model.getName() == null || model.getName().isBlank()) {
            throw new BadRequestException("Nome modello obbligatorio");
        }
        if (model.getBrand() == null) {
            throw new BadRequestException(
                    "Il brand è obbligatorio"
            );
        }
        if (model.getEngineCc() <= 0) {
            throw new BadRequestException(
                    "La cilindrata deve essere maggiore di zero"
            );
        }
        if (model.getCategory() == null) {
            throw new BadRequestException(
                    "Categoria obbligatoria"
            );
        }
        if (model.getHorsePower() <= 0) {
            throw new BadRequestException(
                    "La potenza deve essere maggiore di zero"
            );
        }
        if (model.getWeightKg() <= 0) {
            throw new BadRequestException(
                    "Il peso deve essere maggiore di zero"
            );
        }
        if (model.getYearStart() < 1900) {

            throw new BadRequestException(
                    "Anno di produzione non valido"
            );
        }
        if (model.getYearEnd() != null &&
                model.getYearEnd() < model.getYearStart()) {

            throw new BadRequestException(
                    "L'anno finale non può essere precedente all'anno iniziale"
            );
        }
        if (motorcycleModelRepository.existsByBrandIdAndName(model.getBrand().getId(), model.getName())
        ) {
            throw new BadRequestException(
                    "Modello già presente per questo brand"
            );
        }
        return motorcycleModelRepository.save(model);
    }


    public List<MotorcycleModel> findByBrand(UUID brandId) {
        return motorcycleModelRepository.findByBrandId(brandId);
    }


    public List<MotorcycleModel> searchByName(String name) {
        return motorcycleModelRepository.findByNameContainingIgnoreCase(name);
    }


    public void delete(UUID id) {
        MotorcycleModel model = findById(id);
        motorcycleModelRepository.delete(model);
    }

}
