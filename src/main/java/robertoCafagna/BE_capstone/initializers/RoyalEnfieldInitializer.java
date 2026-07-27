package robertoCafagna.BE_capstone.initializers;


import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import robertoCafagna.BE_capstone.entities.Brand;
import robertoCafagna.BE_capstone.entities.MotorcycleModel;
import robertoCafagna.BE_capstone.enums.MotorcycleCategory;
import robertoCafagna.BE_capstone.repositories.MotorcycleModelRepository;
import robertoCafagna.BE_capstone.services.BrandService;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@AllArgsConstructor
@Order(25)
public class RoyalEnfieldInitializer implements CommandLineRunner {
    private MotorcycleModelRepository motorcycleModelRepository;
    private BrandService brandService;


    @Override
    public void run(String... args) throws Exception {

        if (motorcycleModelRepository.existsByBrandName("Royal Enfield")) {
            return;
        }

        Resource resource =
                new ClassPathResource(
                        "DATA/csv/ModelRoyalEnfield.csv"
                );

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream())
        );
        reader.lines()
                .skip(1)
                .forEach(line -> {
                    String[] data =
                            line.split(",", -1);
                    Brand brand =
                            brandService.findByName(data[0].trim());
                    MotorcycleModel model = new MotorcycleModel(
                            brand,
                            data[1].trim(),
                            parseInt(data[2]),
                            MotorcycleCategory.valueOf(data[3].trim()),
                            parseInt(data[4]),
                            parseInteger(data[5]),
                            parseInteger(data[6]),
                            parseInteger(data[7]),
                            data[8].isBlank() ? null : data[8].trim());
                    motorcycleModelRepository.save(model);
                });
        reader.close();
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value.trim());
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }
}
