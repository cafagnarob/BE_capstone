package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.entities.Brand;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.BrandRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {
    private final BrandRepository brandRepository;

    public Brand findByName(String name) {
        return brandRepository.findByName(name)
                .orElseThrow(() ->
                        new NotFoundException("brand " + name + " non trovato"));
    }

    public Page<Brand> getAll(int page, int size, String orderBy) {

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
        return brandRepository.findAll(pageable);
    }


    public Brand save(Brand brand) {
        if (brand.getName() == null || brand.getName().isBlank()) {
            throw new BadRequestException("Il nome è obbligatorio");
        }
        if (brandRepository.existsByName(brand.getName())) {
            throw new BadRequestException("brand già presente");
        }
        brand.setName(
                brand.getName().trim()
        );
        return brandRepository.save(brand);
    }

    public boolean existsByName(String name) {
        return this.brandRepository.existsByName(name);

    }
}
