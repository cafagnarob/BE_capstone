package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.BrandResponseDTO;
import robertoCafagna.BE_capstone.entities.Brand;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.BrandRepository;

import java.util.List;

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

    public List<BrandResponseDTO> getAll() {
        return brandRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private BrandResponseDTO toDTO(Brand brand) {
        return new BrandResponseDTO(brand.getId(), brand.getName(), brand.getLogoUrl());
    }


}
