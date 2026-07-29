package robertoCafagna.BE_capstone.initializers;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import robertoCafagna.BE_capstone.entities.Brand;
import robertoCafagna.BE_capstone.repositories.GARAGE.BrandRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@AllArgsConstructor
@Order(1)
public class BrandInitializer implements CommandLineRunner {
    private final BrandRepository brandRepository;

    @Override
    public void run(String... args) throws Exception {
        if (brandRepository.count() == 0) {
            Resource resource =
                    new ClassPathResource("DATA/csv/brands.csv");
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    resource.getInputStream()
                            )
                    );
            reader.lines()
                    .skip(1)
                    .forEach(line -> {
                        String[] data = line.split(",", -1);
                        Brand brand = new Brand();
                        brand.setName(data[0].trim());
                        brand.setLogoUrl(data[1].trim());
                        if (brand.getLogoUrl().isEmpty()) {
                            brand.setLogoUrl(null);
                        }
                        brandRepository.save(brand);
                    });

        }

    }
}
